package com.roozyar.app.domain.repository

class BackupFormatException(message: String) : Exception(message)

data class BackupCounts(val tasks: Int, val habits: Int, val goals: Int, val journalEntries: Int, val events: Int)

interface BackupRepository {
    /** Serialises everything stored on the device to a JSON document. */
    suspend fun createBackup(): String

    /** Validates [json] without changing anything. Throws [BackupFormatException] if it cannot be restored. */
    fun inspect(json: String): BackupCounts

    /** Replaces ALL current data with the contents of [json], atomically. Throws [BackupFormatException] before touching data if invalid. */
    suspend fun restore(json: String)
}
