package com.alexcova.perkeo.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.alexcova.perkeo.data.local.dao.SeedDao
import com.alexcova.perkeo.data.local.entity.SeedEntity

@Database(
    entities = [SeedEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class PerkeoDatabase : RoomDatabase() {
    abstract fun seedDao(): SeedDao
}

