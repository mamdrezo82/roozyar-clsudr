package com.roozyar.app.domain.repository

import com.roozyar.app.domain.model.ActiveFocus
import com.roozyar.app.domain.model.FocusSession
import kotlinx.coroutines.flow.Flow

interface FocusRepository {
    fun observeSessions(): Flow<List<FocusSession>>
    suspend fun saveSession(session: FocusSession): Long
    suspend fun deleteSession(id: Long)

    /** The running timer, or null. Survives process death. */
    fun observeActive(): Flow<ActiveFocus?>
    suspend fun setActive(active: ActiveFocus?)
}
