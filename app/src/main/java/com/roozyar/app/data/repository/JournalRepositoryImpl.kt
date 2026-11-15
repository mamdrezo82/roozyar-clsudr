package com.roozyar.app.data.repository

import com.roozyar.app.core.database.JournalDao
import com.roozyar.app.core.database.JournalEntryEntity
import com.roozyar.app.domain.model.JournalEntry
import com.roozyar.app.domain.model.JournalMood
import com.roozyar.app.domain.model.parseTags
import com.roozyar.app.domain.repository.JournalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject

fun JournalEntryEntity.toDomain() = JournalEntry(
    id = id,
    title = title,
    body = body,
    mood = JournalMood.fromName(mood),
    tags = parseTags(tags),
    starred = starred,
    isDraft = isDraft,
    createdAt = Instant.ofEpochMilli(createdAtEpochMillis),
    updatedAt = Instant.ofEpochMilli(updatedAtEpochMillis),
)

fun JournalEntry.toEntity() = JournalEntryEntity(
    id = id,
    title = title,
    body = body,
    mood = mood?.name.orEmpty(),
    tags = tags.joinToString(","),
    starred = starred,
    isDraft = isDraft,
    createdAtEpochMillis = createdAt.toEpochMilli(),
    updatedAtEpochMillis = updatedAt.toEpochMilli(),
)

class JournalRepositoryImpl @Inject constructor(private val dao: JournalDao) : JournalRepository {
    override fun observeEntries(): Flow<List<JournalEntry>> = dao.observeAll().map { l -> l.map { it.toDomain() } }

    override suspend fun getEntry(id: Long): JournalEntry? = dao.getById(id)?.toDomain()

    override suspend fun saveEntry(entry: JournalEntry): Long =
        if (entry.id == 0L) {
            dao.insert(entry.toEntity())
        } else {
            dao.update(entry.toEntity())
            entry.id
        }

    override suspend fun deleteEntry(id: Long) = dao.deleteById(id)

    override suspend fun setStarred(id: Long, starred: Boolean) = dao.setStarred(id, starred)
}
