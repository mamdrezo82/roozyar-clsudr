package com.roozyar.app.core.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** [tags] is a comma-separated list. [mood] is a JournalMood name or empty. */
@Entity(tableName = "journal_entries", indices = [Index(value = ["createdAtEpochMillis"])])
data class JournalEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val body: String,
    val mood: String = "",
    val tags: String = "",
    val starred: Boolean = false,
    val isDraft: Boolean = false,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
)

/** Calendar event. [endEpochMillis] is optional; [hasTime] false means an all-day event on that date. */
@Entity(tableName = "events", indices = [Index(value = ["startEpochMillis"])])
data class EventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val startEpochMillis: Long,
    val endEpochMillis: Long? = null,
    val hasTime: Boolean = false,
    val category: String = "PERSONAL",
    val location: String = "",
)
