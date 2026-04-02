package com.alexcova.perkeo.data

import android.content.Context
import androidx.room.Room
import com.alexcova.perkeo.data.local.PerkeoDatabase
import com.alexcova.perkeo.data.repository.AssetFinderCacheRepository
import com.alexcova.perkeo.data.repository.RoomSeedRepository
import com.alexcova.perkeo.domain.repository.FinderCacheRepository
import com.alexcova.perkeo.domain.repository.SeedRepository

class AppGraph(context: Context) {
    private val db: PerkeoDatabase = Room.databaseBuilder(
        context,
        PerkeoDatabase::class.java,
        "perkeo.db",
    ).build()

    val seedRepository: SeedRepository = RoomSeedRepository(db.seedDao())
    val finderCacheRepository: FinderCacheRepository = AssetFinderCacheRepository(context)
}

