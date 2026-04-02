package com.alexcova.perkeo.data.repository

import com.alexcova.perkeo.data.local.dao.SeedDao
import com.alexcova.perkeo.data.mapper.toDomain
import com.alexcova.perkeo.data.mapper.toEntity
import com.alexcova.perkeo.domain.model.SavedSeed
import com.alexcova.perkeo.domain.repository.SeedRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomSeedRepository(
    private val seedDao: SeedDao,
) : SeedRepository {
    override fun observeSavedSeeds(): Flow<List<SavedSeed>> {
        return seedDao.observeAll().map { seeds -> seeds.map { it.toDomain() } }
    }

    override suspend fun saveSeed(seed: SavedSeed) {
        seedDao.upsert(seed.toEntity())
    }

    override suspend fun removeSeed(seed: String) {
        seedDao.deleteBySeed(seed)
    }

    override suspend fun isSaved(seed: String): Boolean {
        return seedDao.findBySeed(seed) != null
    }
}

