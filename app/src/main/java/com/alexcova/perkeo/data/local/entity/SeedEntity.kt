package com.alexcova.perkeo.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_seeds")
data class SeedEntity(
    @PrimaryKey val seed: String,
    val timestamp: Long,
    val title: String? = null,
    val level: String? = null,
    val score: Int? = null,
)

