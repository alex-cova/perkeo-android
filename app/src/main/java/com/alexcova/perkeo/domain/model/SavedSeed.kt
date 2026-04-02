package com.alexcova.perkeo.domain.model

data class SavedSeed(
    val seed: String,
    val timestamp: Long,
    val title: String? = null,
    val level: String? = null,
    val score: Int? = null,
)

