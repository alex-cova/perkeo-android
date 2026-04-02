package com.alexcova.perkeo.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.alexcova.perkeo.data.local.entity.SeedEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SeedDao {
    @Query("SELECT * FROM saved_seeds ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<SeedEntity>>

    @Query("SELECT * FROM saved_seeds WHERE seed = :seed LIMIT 1")
    suspend fun findBySeed(seed: String): SeedEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(seed: SeedEntity)

    @Delete
    suspend fun delete(seed: SeedEntity)

    @Query("DELETE FROM saved_seeds WHERE seed = :seed")
    suspend fun deleteBySeed(seed: String)
}

