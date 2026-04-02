package com.alexcova.perkeo.features.analyzer

import com.alexcova.perkeo.domain.engine.*

enum class ToastStyle { ERROR, WARNING, SUCCESS, INFO }

data class ToastData(val style: ToastStyle, val message: String, val duration: Long = 3000)

data class AnalyzerUiState(
    val seedInput: String = "",
    val normalizedSeed: String = "",
    val isLoading: Boolean = false,
    val maxAnte: Int = 8,
    val startingAnte: Int = 1,
    val showman: Boolean = false,
    val autoBuyVoucher: Boolean = true,
    val deck: Deck = Deck.RED_DECK,
    val stake: Stake = Stake.White_Stake,
    val disabledItems: List<Item> = emptyList(),
    val run: Run? = null,
    val showInput: Boolean = false,
    val showConfig: Boolean = false,
    val showSummary: Boolean = false,
    val showSaveView: Boolean = false,
    val toast: ToastData? = null,
) {
    val title: String get() = if (seedInput.isEmpty()) "WELCOME" else seedInput
    val firstAnte: Int get() = run?.antes?.firstOrNull()?.ante ?: startingAnte
}


