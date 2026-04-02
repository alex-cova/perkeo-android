package com.alexcova.perkeo.features.saved

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alexcova.perkeo.domain.model.SavedSeed
import com.alexcova.perkeo.domain.repository.SeedRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SavedSeedsViewModel(
    private val seedRepository: SeedRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SavedSeedsUiState())
    val uiState: StateFlow<SavedSeedsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            seedRepository.observeSavedSeeds().collect { seeds ->
                _uiState.update {
                    it.copy(
                        seeds = seeds,
                        isLoading = false,
                    )
                }
            }
        }
    }

    fun saveSeed(seed: String) {
        viewModelScope.launch {
            val normalized = seed.trim().uppercase()
            if (normalized.isEmpty()) return@launch
            seedRepository.saveSeed(
                SavedSeed(
                    seed = normalized,
                    timestamp = System.currentTimeMillis(),
                ),
            )
        }
    }

    fun deleteSeed(seed: String) {
        viewModelScope.launch {
            seedRepository.removeSeed(seed)
        }
    }
}

