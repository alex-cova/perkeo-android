package com.alexcova.perkeo.features.analyzer

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alexcova.perkeo.domain.engine.*
import com.alexcova.perkeo.domain.model.SavedSeed
import com.alexcova.perkeo.domain.repository.SeedRepository
import com.alexcova.perkeo.domain.util.DailySeedGenerator
import com.alexcova.perkeo.domain.util.SeedFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AnalyzerViewModel(
    private val seedRepository: SeedRepository? = null,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AnalyzerUiState())
    val uiState: StateFlow<AnalyzerUiState> = _uiState.asStateFlow()

    private var analyzeJob: Job? = null

    fun onSeedInputChanged(value: String) {
        val filtered = value.filter { it.isLetterOrDigit() }.take(8).uppercase()
        _uiState.update { it.copy(seedInput = filtered, normalizedSeed = filtered) }
    }

    fun randomSeed() {
        val generated = BalatroAnalyzer.generateRandomString()
        _uiState.update { it.copy(seedInput = generated, normalizedSeed = generated) }
        analyze()
    }

    fun seedOfTheDay() {
        val seed = DailySeedGenerator.generate()
        _uiState.update { it.copy(seedInput = seed, normalizedSeed = seed) }
        analyze()
    }

    fun paste(context: Context) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val text = clipboard?.primaryClip?.getItemAt(0)?.text?.toString() ?: return
        if (SeedFormat.isValid(text)) {
            val normalized = SeedFormat.normalize(text)
            _uiState.update { it.copy(seedInput = normalized, normalizedSeed = normalized, showConfig = false) }
            analyze()
        } else {
            showToast(ToastStyle.ERROR, "Not a valid seed in the clipboard")
        }
    }

    fun copy(context: Context, seed: String? = null) {
        val s = seed ?: _uiState.value.seedInput
        if (s.isEmpty()) return
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        clipboard?.setPrimaryClip(ClipData.newPlainText("seed", s.uppercase()))
        showToast(ToastStyle.SUCCESS, "Seed $s copied to clipboard")
        _uiState.update { it.copy(showConfig = false) }
    }

    fun enterSeed() { _uiState.update { it.copy(showInput = !it.showInput) } }
    fun toggleConfig() { _uiState.update { it.copy(showConfig = !it.showConfig) } }
    fun toggleSummary() { _uiState.update { it.copy(showSummary = !it.showSummary) } }
    fun dismissInput() { _uiState.update { it.copy(showInput = false) } }
    fun dismissConfig() { _uiState.update { it.copy(showConfig = false) } }
    fun dismissSummary() { _uiState.update { it.copy(showSummary = false) } }
    fun dismissSaveView() { _uiState.update { it.copy(showSaveView = false) } }
    fun dismissToast() { _uiState.update { it.copy(toast = null) } }

    fun showToast(style: ToastStyle, message: String) {
        _uiState.update { it.copy(toast = ToastData(style, message)) }
    }

    fun storeSeed() {
        _uiState.update { it.copy(showConfig = false, showSaveView = true) }
    }

    fun saveSeed(level: JokerType, title: String) {
        viewModelScope.launch {
            val state = _uiState.value
            seedRepository?.saveSeed(SavedSeed(seed = state.seedInput, timestamp = System.currentTimeMillis(), title = title, level = level.rawValue, score = state.run?.score))
            showToast(ToastStyle.INFO, "Seed ${state.seedInput} saved")
            _uiState.update { it.copy(showSaveView = false) }
        }
    }

    fun isSelected(joker: Item): Boolean {
        val state = _uiState.value
        if (state.showman && joker.rawValue == UnCommonJoker.Showman.rawValue) return true
        return state.disabledItems.any { it.rawValue == joker.rawValue }
    }

    fun toggleDisabledItem(joker: Item) {
        _uiState.update { state ->
            val current = state.disabledItems
            val newList = if (current.any { it.rawValue == joker.rawValue }) current.filter { it.rawValue != joker.rawValue }
                          else current + joker
            state.copy(disabledItems = newList)
        }
    }

    fun setMaxAnte(value: Int) { _uiState.update { it.copy(maxAnte = value.coerceIn(1, 30)) } }
    fun setStartingAnte(value: Int) { _uiState.update { it.copy(startingAnte = value.coerceIn(1, 29)) } }
    fun setShowman(value: Boolean) { _uiState.update { it.copy(showman = value) } }
    fun setAutoBuyVoucher(value: Boolean) { _uiState.update { it.copy(autoBuyVoucher = value) } }
    fun setDeck(deck: Deck) { _uiState.update { it.copy(deck = deck) } }
    fun setStake(stake: Stake) { _uiState.update { it.copy(stake = stake) } }

    fun changeSeed(seed: String) {
        _uiState.update { it.copy(startingAnte = 1, maxAnte = 8) }
        if (_uiState.value.seedInput == seed) {
            if (_uiState.value.run == null) analyze()
            return
        }
        _uiState.update { it.copy(run = null, seedInput = seed, normalizedSeed = seed) }
        analyze()
    }

    fun analyze() {
        val state = _uiState.value
        if (state.isLoading || state.seedInput.isEmpty()) return
        _uiState.update { it.copy(isLoading = true, showInput = false) }
        analyzeJob?.cancel()
        analyzeJob = viewModelScope.launch {
            val run = withContext(Dispatchers.Default) {
                BalatroAnalyzer.performAnalysis(
                    seed = state.seedInput.uppercase(),
                    maxDepth = state.maxAnte,
                    startingAnte = state.startingAnte,
                    deck = state.deck,
                    stake = state.stake,
                    showman = state.showman,
                    disabledItems = state.disabledItems,
                    autoBuyVoucher = state.autoBuyVoucher,
                )
            }
            _uiState.update { it.copy(run = run, isLoading = false) }
        }
    }
}

