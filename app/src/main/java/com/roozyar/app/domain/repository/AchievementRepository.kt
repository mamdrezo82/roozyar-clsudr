package com.roozyar.app.domain.repository

import kotlinx.coroutines.flow.Flow
import java.time.Instant

interface AchievementRepository {
    /** code -> the moment it was first unlocked. */
    fun observeUnlocked(): Flow<Map<String, Instant>>
    suspend fun unlock(code: String, at: Instant)
}
