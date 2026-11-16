package com.roozyar.app.data.repository

import com.roozyar.app.core.database.EventDao
import com.roozyar.app.core.database.EventEntity
import com.roozyar.app.domain.model.Event
import com.roozyar.app.domain.model.EventCategory
import com.roozyar.app.domain.repository.EventRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject

fun EventEntity.toDomain() = Event(
    id = id,
    title = title,
    start = Instant.ofEpochMilli(startEpochMillis),
    end = endEpochMillis?.let(Instant::ofEpochMilli),
    hasTime = hasTime,
    category = EventCategory.fromName(category),
    location = location,
)

fun Event.toEntity() = EventEntity(
    id = id,
    title = title,
    startEpochMillis = start.toEpochMilli(),
    endEpochMillis = end?.toEpochMilli(),
    hasTime = hasTime,
    category = category.name,
    location = location,
)

class EventRepositoryImpl @Inject constructor(private val dao: EventDao) : EventRepository {
    override fun observeEvents(): Flow<List<Event>> = dao.observeAll().map { l -> l.map { it.toDomain() } }

    override suspend fun getEvent(id: Long): Event? = dao.getById(id)?.toDomain()

    override suspend fun saveEvent(event: Event): Long =
        if (event.id == 0L) {
            dao.insert(event.toEntity())
        } else {
            dao.update(event.toEntity())
            event.id
        }

    override suspend fun deleteEvent(id: Long) = dao.deleteById(id)
}
