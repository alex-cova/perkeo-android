package com.alexcova.perkeo.data.mapper

import com.alexcova.perkeo.data.local.entity.SeedEntity
import com.alexcova.perkeo.domain.model.SavedSeed

fun SeedEntity.toDomain(): SavedSeed = SavedSeed(
    seed = seed,
    timestamp = timestamp,
    title = title,
    level = level,
    score = score,
)

fun SavedSeed.toEntity(): SeedEntity = SeedEntity(
    seed = seed,
    timestamp = timestamp,
    title = title,
    level = level,
    score = score,
)

