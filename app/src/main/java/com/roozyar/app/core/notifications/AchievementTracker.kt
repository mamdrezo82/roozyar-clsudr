package com.roozyar.app.core.notifications

import com.roozyar.app.domain.model.AchievementCatalog
import com.roozyar.app.domain.repository.AchievementRepository
import com.roozyar.app.domain.usecase.ObserveActivityStatsUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.Clock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Records achievements the moment their rule becomes true while the app process is alive. Achievements already
 * satisfied by data that existed before this feature (or before a backup restore) are stamped with the time they
 * are first noticed, not the time they were truly earned.
 */
@Singleton
class AchievementTracker @Inject constructor(
    private val stats: ObserveActivityStatsUseCase,
    private val repository: AchievementRepository,
    private val clock: Clock,
) {
    fun start(scope: CoroutineScope) {
        scope.launch {
            try {
                combine(stats(), repository.observeUnlocked()) { s, unlocked -> AchievementCatalog.newlyUnlocked(s, unlocked.keys) }
                    .collect { fresh -> fresh.forEach { repository.unlock(it.code, clock.instant()) } }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
            }
        }
    }
}
