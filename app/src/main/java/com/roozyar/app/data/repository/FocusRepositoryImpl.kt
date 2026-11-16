package com.roozyar.app.data.repository

import com.roozyar.app.core.database.FocusDao
import com.roozyar.app.core.database.FocusSessionEntity
import com.roozyar.app.core.datastore.FocusActiveStore
import com.roozyar.app.domain.model.ActiveFocus
import com.roozyar.app.domain.model.FocusSession
import com.roozyar.app.domain.repository.FocusRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject

fun FocusSessionEntity.toDomain() = FocusSession(
    id = id, startedAt = Instant.ofEpochMilli(startEpochMillis), plannedMinutes = plannedMinutes,
    actualSeconds = actualSeconds, completed = completed, taskId = taskId, goalId = goalId,
)

fun FocusSession.toEntity() = FocusSessionEntity(
    id = id, startEpochMillis = startedAt.toEpochMilli(), plannedMinutes = plannedMinutes,
    actualSeconds = actualSeconds, completed = completed, taskId = taskId, goalId = goalId,
)

class FocusRepositoryImpl @Inject constructor(
    private val dao: FocusDao,
    private val store: FocusActiveStore,
) : FocusRepository {
    override fun observeSessions(): Flow<List<FocusSession>> = dao.observeSessions().map { l -> l.map { it.toDomain() } }

    override suspend fun saveSession(session: FocusSession): Long = dao.insert(session.toEntity())

    override suspend fun deleteSession(id: Long) = dao.deleteById(id)

    override fun observeActive(): Flow<ActiveFocus?> = store.active

    override suspend fun setActive(active: ActiveFocus?) = store.set(active)
}
