package com.alexcova.perkeo.domain.repository

import com.alexcova.perkeo.domain.model.SavedSeed
import kotlinx.coroutines.flow.Flow

interface SeedRepository {
    fun observeSavedSeeds(): Flow<List<SavedSeed>>
    suspend fun saveSeed(seed: SavedSeed)
    suspend fun removeSeed(seed: String)
    suspend fun isSaved(seed: String): Boolean
}

