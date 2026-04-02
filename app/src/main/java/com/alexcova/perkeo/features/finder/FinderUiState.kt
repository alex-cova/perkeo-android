package com.alexcova.perkeo.features.finder

import com.alexcova.perkeo.domain.engine.DraggableItem

data class FinderUiState(
    val useLegendarySearch: Boolean = false,
    val useInstantSearch: Boolean = false,
    val loadingCache: Boolean = false,
    val cachedLegendaryCount: Int = 0,
    val cachedInstantCount: Int = 0,
    // JAML clause state
    val must: List<DraggableItem> = emptyList(),
    val should: List<DraggableItem> = emptyList(),
    val mustNot: List<DraggableItem> = emptyList(),
    // Search progress
    val searching: Boolean = false,
    val foundSeeds: Map<String, Int> = emptyMap(),
    val processedCount: Int = 0,
    val seedsFoundCount: Int = 0,
    // Heavy search controls
    val seedsToAnalyze: Int = 1_000_000,
    val startingAnte: Int = 1,
    val maxAnte: Int = 1,
    // UI
    val showFoundSeeds: Boolean = true,
    val showHeavyControls: Boolean = false,
) {
    val isEmpty: Boolean get() = must.isEmpty() && should.isEmpty() && mustNot.isEmpty()
    val isHeavySearch: Boolean get() = maxAnte - startingAnte > 8
}
