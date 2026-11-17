package com.roozyar.app.domain.usecase

import com.roozyar.app.domain.model.ActivityStats
import com.roozyar.app.domain.model.computeActivityStats
import com.roozyar.app.domain.repository.FocusRepository
import com.roozyar.app.domain.repository.GoalRepository
import com.roozyar.app.domain.repository.HabitRepository
import com.roozyar.app.domain.repository.JournalRepository
import com.roozyar.app.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

/** Live aggregate of everything XP and achievements are computed from. */
class ObserveActivityStatsUseCase @Inject constructor(
    private val tasks: TaskRepository,
    private val goals: GoalRepository,
    private val journal: JournalRepository,
    private val habits: HabitRepository,
    private val focus: FocusRepository,
    private val clock: Clock,
) {
    operator fun invoke(): Flow<ActivityStats> = combine(
        combine(tasks.observeTasks(), goals.observeGoals(), journal.observeEntries()) { t, g, j -> Triple(t, g, j) },
        combine(habits.observeHabits(), habits.observeLogsFrom(LocalDate.ofEpochDay(0))) { h, l -> h to l },
        focus.observeSessions(),
    ) { (t, g, j), (h, l), f ->
        computeActivityStats(t, g, j, h, l, f, LocalDate.now(clock), clock.zone)
    }
}
