package com.roozyar.app.data.repository

import com.roozyar.app.core.database.FocusDao
import com.roozyar.app.core.database.UnlockedAchievementEntity
import com.roozyar.app.domain.repository.AchievementRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject

class AchievementRepositoryImpl @Inject constructor(private val dao: FocusDao) : AchievementRepository {
    override fun observeUnlocked(): Flow<Map<String, Instant>> =
        dao.observeUnlocked().map { l -> l.associate { it.code to Instant.ofEpochMilli(it.unlockedAtEpochMillis) } }

    override suspend fun unlock(code: String, at: Instant) = dao.unlock(UnlockedAchievementEntity(code, at.toEpochMilli()))
}
