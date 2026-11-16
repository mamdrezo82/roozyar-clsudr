package com.roozyar.app.domain.repository

import com.roozyar.app.domain.model.Event
import kotlinx.coroutines.flow.Flow

interface EventRepository {
    fun observeEvents(): Flow<List<Event>>
    suspend fun getEvent(id: Long): Event?

    /** Inserts when [Event.id] is 0, otherwise updates. */
    suspend fun saveEvent(event: Event): Long
    suspend fun deleteEvent(id: Long)
}
