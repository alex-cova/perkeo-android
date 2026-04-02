package com.alexcova.perkeo.features.saved

import com.alexcova.perkeo.domain.model.SavedSeed

data class SavedSeedsUiState(
    val seeds: List<SavedSeed> = emptyList(),
    val isLoading: Boolean = true,
)

