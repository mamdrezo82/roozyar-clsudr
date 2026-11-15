package com.roozyar.app.domain.repository

import com.roozyar.app.domain.model.JournalEntry
import kotlinx.coroutines.flow.Flow

interface JournalRepository {
    fun observeEntries(): Flow<List<JournalEntry>>
    suspend fun getEntry(id: Long): JournalEntry?

    /** Inserts when [JournalEntry.id] is 0, otherwise updates. */
    suspend fun saveEntry(entry: JournalEntry): Long
    suspend fun deleteEntry(id: Long)
    suspend fun setStarred(id: Long, starred: Boolean)
}
