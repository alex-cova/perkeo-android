package com.alexcova.perkeo.features.finder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alexcova.perkeo.domain.engine.*
import com.alexcova.perkeo.domain.model.finder.CompressedSeed
import com.alexcova.perkeo.domain.model.finder.FinderDataItem
import com.alexcova.perkeo.domain.repository.FinderCacheRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

class FinderViewModel(
    private val finderCacheRepository: FinderCacheRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(FinderUiState())
    val uiState: StateFlow<FinderUiState> = _uiState.asStateFlow()

    private val running = AtomicBoolean(false)
    private var searchJobs: List<Job> = emptyList()

    // ── Cache toggles ──

    fun toggleLegendary(enabled: Boolean) {
        _uiState.update { it.copy(useLegendarySearch = enabled, useInstantSearch = false) }
        if (enabled) loadLegendaryCache()
    }

    fun toggleInstant(enabled: Boolean) {
        _uiState.update { it.copy(useInstantSearch = enabled, useLegendarySearch = false) }
        if (enabled) loadInstantCache()
    }

    private fun loadLegendaryCache() {
        viewModelScope.launch {
            _uiState.update { it.copy(loadingCache = true) }
            val data = finderCacheRepository.readJokerData()
            _uiState.update { it.copy(loadingCache = false, cachedLegendaryCount = data.size) }
        }
    }

    private fun loadInstantCache() {
        viewModelScope.launch {
            _uiState.update { it.copy(loadingCache = true) }
            val data = finderCacheRepository.readInstant()
            _uiState.update { it.copy(loadingCache = false, cachedInstantCount = data.size) }
        }
    }

    // ── Heavy search controls ──

    fun toggleHeavyControls() {
        _uiState.update { it.copy(showHeavyControls = !it.showHeavyControls) }
    }

    fun incrementSeedsToAnalyze() {
        _uiState.update { it.copy(seedsToAnalyze = it.seedsToAnalyze + 10_000) }
    }

    fun decrementSeedsToAnalyze() {
        _uiState.update { it.copy(seedsToAnalyze = maxOf(10_000, it.seedsToAnalyze - 10_000)) }
    }

    fun incrementStartingAnte() {
        _uiState.update { s ->
            val next = minOf(29, s.startingAnte + 1)
            s.copy(startingAnte = next, maxAnte = maxOf(s.maxAnte, next))
        }
    }

    fun decrementStartingAnte() {
        _uiState.update { it.copy(startingAnte = maxOf(1, it.startingAnte - 1)) }
    }

    fun incrementMaxAnte() {
        _uiState.update { it.copy(maxAnte = minOf(30, it.maxAnte + 1)) }
    }

    fun decrementMaxAnte() {
        _uiState.update { s ->
            val next = maxOf(1, s.maxAnte - 1)
            s.copy(maxAnte = next, startingAnte = minOf(s.startingAnte, maxOf(next, 1)))
        }
    }

    // ── JAML clause management ──

    fun addToClause(item: DraggableItem, clause: ClauseType) {
        _uiState.update { s ->
            val cleaned = s.removeFromAll(item.id)
            val updated = item.copy(score = if (clause == ClauseType.SHOULD && item.score == 0.0) 1.0 else item.score)
            when (clause) {
                ClauseType.MUST -> cleaned.copy(must = cleaned.must + updated)
                ClauseType.SHOULD -> cleaned.copy(should = cleaned.should + updated)
                ClauseType.MUST_NOT -> cleaned.copy(mustNot = cleaned.mustNot + updated)
            }
        }
    }

    fun removeClause(id: String) {
        _uiState.update { it.removeFromAll(id) }
    }

    fun clearAll() {
        _uiState.update { it.copy(must = emptyList(), should = emptyList(), mustNot = emptyList(), foundSeeds = emptyMap()) }
    }

    fun toggleFoundSeeds() {
        _uiState.update { it.copy(showFoundSeeds = !it.showFoundSeeds) }
    }

    // ── Search ──

    fun stopSearch() {
        running.set(false)
        searchJobs.forEach { it.cancel() }
        _uiState.update { it.copy(searching = false) }
    }

    fun startSearch() {
        if (running.get()) return
        val state = _uiState.value
        _uiState.update { it.copy(searching = true, foundSeeds = emptyMap(), processedCount = 0, seedsFoundCount = 0) }

        if (state.useLegendarySearch || state.useInstantSearch) {
            runCacheSearch()
        } else {
            runHeavySearch()
        }
    }

    private fun runCacheSearch() {
        viewModelScope.launch(Dispatchers.Default) {
            val state = _uiState.value
            val must = state.must.map { it.item() }
            val should = state.should.map { it.item() }
            val mustNot = state.mustNot.map { it.item() }

            val result = if (state.useInstantSearch) {
                searchCompressed(must, should, mustNot)
            } else {
                searchFinderData(must, should, mustNot)
            }

            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(searching = false, foundSeeds = result, seedsFoundCount = result.size) }
            }
        }
    }

    private suspend fun searchCompressed(must: List<Item>, should: List<Item>, mustNot: List<Item>): Map<String, Int> {
        val compressed = finderCacheRepository.readInstant()
        val result = mutableMapOf<String, Int>()

        val mustStored = must.filterIsInstance<StoredItem>()
        val shouldStored = should.filterIsInstance<StoredItem>()
        val mustNotStored = mustNot.filterIsInstance<StoredItem>()

        for (seed in compressed) {
            if (result.size >= 200) break
            if (mustNotStored.any { seed.getBit(it.index) }) continue
            if (mustStored.any { !seed.getBit(it.index) }) continue
            val score = shouldStored.count { seed.getBit(it.index) }
            result[seed.seedString()] = score
        }

        return result
    }

    private suspend fun searchFinderData(must: List<Item>, should: List<Item>, mustNot: List<Item>): Map<String, Int> {
        val jokerData = finderCacheRepository.readJokerData()
        val result = mutableMapOf<String, Int>()

        for (item in jokerData) {
            if (result.size >= 200) break
            if (mustNot.any { item.isOn(it) }) continue
            if (must.any { !item.isOn(it) }) continue
            val score = item.score + should.count { item.isOn(it) }
            result[item.seed] = score
        }

        return result
    }

    private fun runHeavySearch() {
        val state = _uiState.value
        val total = state.seedsToAnalyze
        val jobs = 3
        val split = total / jobs

        running.set(true)
        val finished = AtomicInteger(0)
        val processed = AtomicInteger(0)

        searchJobs = (0 until jobs).map {
            viewModelScope.launch(Dispatchers.Default) {
                val foundLocal = mutableSetOf<String>()
                val must = state.must
                val should = state.should
                val mustNot = state.mustNot
                val allSelections = (must + should).map { it.item() }

                for (i in 0 until split) {
                    if (!running.get()) break
                    if (foundLocal.size > 25) break

                    val seed = BalatroAnalyzer.generateRandomString()
                    val run = BalatroAnalyzer.configureForSpeedAnalysis(
                        seed, state.maxAnte, state.startingAnte, allSelections
                    )

                    if (mustNot.any { run.contains(it.item()) }) continue
                    if (must.all { run.contains(it.item()) }) foundLocal.add(seed)

                    if (i % 2000 == 0) {
                        val delta = 2000
                        val newProcessed = processed.addAndGet(delta)
                        val newFound = foundLocal.size
                        withContext(Dispatchers.Main) {
                            _uiState.update { it.copy(processedCount = newProcessed, seedsFoundCount = newFound) }
                        }
                    }
                }

                val finishedCount = finished.incrementAndGet()
                withContext(Dispatchers.Main) {
                    _uiState.update { s ->
                        val newMap = s.foundSeeds.toMutableMap()
                        for (seed in foundLocal) newMap[seed] = 0
                        s.copy(foundSeeds = newMap, seedsFoundCount = newMap.size)
                    }
                    if (finishedCount >= jobs) {
                        running.set(false)
                        _uiState.update { it.copy(searching = false) }
                    }
                }
            }
        }
    }

    // ── Unused legacy ──
    fun onQueryChange(query: String) {}
}

// ── Extensions ──

private fun FinderUiState.removeFromAll(id: String) = copy(
    must = must.filter { it.id != id },
    should = should.filter { it.id != id },
    mustNot = mustNot.filter { it.id != id },
)

private fun FinderDataItem.isOn(item: Item): Boolean {
    for (value in data) {
        val yIndex = (value ushr 24) and 0xFF
        if (yIndex != item.y) continue
        val ord = (value ushr 16) and 0xFF
        if (ord != item.bitOrdinal) continue
        return true
    }
    return false
}

private fun CompressedSeed.seedString(): String {
    val lo = memory[0]
    return buildString {
        val chars = "123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        var n = lo.toUInt().toLong() and 0xFFFFFFFFL
        repeat(8) {
            append(chars[(n % chars.length).toInt()])
            n /= chars.length
        }
    }
}
