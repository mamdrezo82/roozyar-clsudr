package com.roozyar.app.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface JournalDao {
    @Query("SELECT * FROM journal_entries ORDER BY createdAtEpochMillis DESC")
    fun observeAll(): Flow<List<JournalEntryEntity>>

    @Query("SELECT * FROM journal_entries WHERE id = :id")
    suspend fun getById(id: Long): JournalEntryEntity?

    @Insert
    suspend fun insert(entry: JournalEntryEntity): Long

    @Update
    suspend fun update(entry: JournalEntryEntity)

    @Query("DELETE FROM journal_entries WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE journal_entries SET starred = :starred WHERE id = :id")
    suspend fun setStarred(id: Long, starred: Boolean)
}
