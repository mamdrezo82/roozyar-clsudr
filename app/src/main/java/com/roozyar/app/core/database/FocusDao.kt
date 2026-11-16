package com.roozyar.app.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FocusDao {
    @Query("SELECT * FROM focus_sessions ORDER BY startEpochMillis DESC")
    fun observeSessions(): Flow<List<FocusSessionEntity>>

    @Insert
    suspend fun insert(session: FocusSessionEntity): Long

    @Query("DELETE FROM focus_sessions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM achievements_unlocked ORDER BY unlockedAtEpochMillis DESC")
    fun observeUnlocked(): Flow<List<UnlockedAchievementEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun unlock(item: UnlockedAchievementEntity)
}
