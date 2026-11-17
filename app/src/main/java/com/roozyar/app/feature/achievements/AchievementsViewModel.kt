package com.roozyar.app.feature.achievements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roozyar.app.domain.model.AchievementCatalog
import com.roozyar.app.domain.model.AchievementDef
import com.roozyar.app.domain.model.ActivityStats
import com.roozyar.app.domain.model.LevelProgress
import com.roozyar.app.domain.model.Xp
import com.roozyar.app.domain.repository.AchievementRepository
import com.roozyar.app.domain.usecase.ObserveActivityStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.Instant
import javax.inject.Inject

data class AchievementItem(val def: AchievementDef, val value: Int, val unlockedAt: Instant?) {
    val unlocked: Boolean get() = unlockedAt != null
    val fraction: Float get() = (value.toFloat() / def.target).coerceIn(0f, 1f)
}

data class AchievementsUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val level: LevelProgress = Xp.progress(0),
    val items: List<AchievementItem> = emptyList(),
) {
    val unlockedCount: Int get() = items.count { it.unlocked }
}

/** Unlocked first (newest first), then locked ones closest to completion first. */
fun buildAchievementItems(stats: ActivityStats, unlocked: Map<String, Instant>): List<AchievementItem> {
    val all = AchievementCatalog.all.map { AchievementItem(it, it.value(stats), unlocked[it.code]) }
    val done = all.filter { it.unlocked }.sortedByDescending { it.unlockedAt }
    val todo = all.filter { !it.unlocked }.sortedByDescending { it.fraction }
    return done + todo
}

@HiltViewModel
class AchievementsViewModel @Inject constructor(
    stats: ObserveActivityStatsUseCase,
    repository: AchievementRepository,
) : ViewModel() {
    val uiState: StateFlow<AchievementsUiState> = combine(stats(), repository.observeUnlocked()) { s, unlocked ->
        AchievementsUiState(isLoading = false, level = Xp.progress(Xp.total(s)), items = buildAchievementItems(s, unlocked))
    }
        .catch { emit(AchievementsUiState(isLoading = false, error = "خواندن دستاوردها ممکن نشد.")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AchievementsUiState())
}
