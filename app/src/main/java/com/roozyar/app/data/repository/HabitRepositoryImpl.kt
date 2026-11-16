package com.roozyar.app.data.repository

import com.roozyar.app.core.database.HabitDao
import com.roozyar.app.core.database.HabitEntity
import com.roozyar.app.core.database.HabitLogEntity
import com.roozyar.app.domain.model.Habit
import com.roozyar.app.domain.model.HabitLog
import com.roozyar.app.domain.repository.HabitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject

fun weekdaysFromMask(mask: Int): Set<DayOfWeek> = DayOfWeek.entries.filter { mask and (1 shl (it.value - 1)) != 0 }.toSet()

fun weekdaysToMask(days: Set<DayOfWeek>): Int = days.fold(0) { acc, d -> acc or (1 shl (d.value - 1)) }

fun HabitEntity.toDomain() = Habit(
    id = id,
    title = title,
    icon = icon,
    weekdays = weekdaysFromMask(weekdaysMask),
    targetCount = targetCount,
    unit = unit,
    createdAt = Instant.ofEpochMilli(createdAtEpochMillis),
    reminderMinuteOfDay = reminderMinuteOfDay,
)

fun Habit.toEntity() = HabitEntity(
    id = id,
    title = title,
    icon = icon,
    weekdaysMask = weekdaysToMask(weekdays),
    targetCount = targetCount,
    unit = unit,
    createdAtEpochMillis = createdAt.toEpochMilli(),
    reminderMinuteOfDay = reminderMinuteOfDay,
)

class HabitRepositoryImpl @Inject constructor(private val dao: HabitDao) : HabitRepository {
    override fun observeHabits(): Flow<List<Habit>> = dao.observeHabits().map { l -> l.map { it.toDomain() } }

    override fun observeLogsFrom(from: LocalDate): Flow<List<HabitLog>> =
        dao.observeLogsFrom(from.toEpochDay()).map { l ->
            l.map { HabitLog(it.habitId, LocalDate.ofEpochDay(it.epochDay), it.count) }
        }

    override suspend fun getHabit(id: Long): Habit? = dao.getById(id)?.toDomain()

    override suspend fun saveHabit(habit: Habit): Long =
        if (habit.id == 0L) {
            dao.insert(habit.toEntity())
        } else {
            dao.update(habit.toEntity())
            habit.id
        }

    override suspend fun deleteHabit(id: Long) = dao.deleteById(id)

    override suspend fun setProgress(habitId: Long, date: LocalDate, count: Int) {
        if (count <= 0) dao.deleteLog(habitId, date.toEpochDay())
        else dao.upsertLog(HabitLogEntity(habitId = habitId, epochDay = date.toEpochDay(), count = count))
    }
}
