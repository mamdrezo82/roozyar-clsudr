package com.roozyar.app.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    @Query("SELECT * FROM events ORDER BY startEpochMillis ASC")
    fun observeAll(): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE id = :id")
    suspend fun getById(id: Long): EventEntity?

    @Insert
    suspend fun insert(event: EventEntity): Long

    @Update
    suspend fun update(event: EventEntity)

    @Query("DELETE FROM events WHERE id = :id")
    suspend fun deleteById(id: Long)
}
