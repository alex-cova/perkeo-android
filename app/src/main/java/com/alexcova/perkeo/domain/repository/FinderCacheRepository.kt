package com.alexcova.perkeo.domain.repository

import com.alexcova.perkeo.domain.model.finder.CompressedSeed
import com.alexcova.perkeo.domain.model.finder.FinderDataItem

interface FinderCacheRepository {
    suspend fun readInstant(): List<CompressedSeed>
    suspend fun readJokerData(): List<FinderDataItem>
}

