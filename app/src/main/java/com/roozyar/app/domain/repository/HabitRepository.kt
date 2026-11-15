package com.roozyar.app.domain.repository

import com.roozyar.app.domain.model.Habit
import com.roozyar.app.domain.model.HabitLog
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface HabitRepository {
    fun observeHabits(): Flow<List<Habit>>
    fun observeLogsFrom(from: LocalDate): Flow<List<HabitLog>>
    suspend fun getHabit(id: Long): Habit?

    /** Inserts when [Habit.id] is 0, otherwise updates. */
    suspend fun saveHabit(habit: Habit): Long
    suspend fun deleteHabit(id: Long)

    /** Sets the day's progress; a [count] of 0 or less clears it. */
    suspend fun setProgress(habitId: Long, date: LocalDate, count: Int)
}
