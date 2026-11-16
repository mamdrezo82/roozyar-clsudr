package com.roozyar.app.domain.model

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class FocusSession(
    val id: Long,
    val startedAt: Instant,
    val plannedMinutes: Int,
    val actualSeconds: Int,
    val completed: Boolean,
    val taskId: Long?,
    val goalId: Long?,
) {
    val actualMinutes: Int get() = actualSeconds / 60
    fun date(zone: ZoneId): LocalDate = startedAt.atZone(zone).toLocalDate()
}

enum class FocusKind { WORK, BREAK }

/** The timer that is currently running; persisted so it survives the app being closed. */
data class ActiveFocus(
    val kind: FocusKind,
    val startedAt: Instant,
    val endsAt: Instant,
    val plannedMinutes: Int,
    val taskId: Long?,
    val goalId: Long?,
) {
    fun remainingSeconds(now: Instant): Long = maxOf(0, endsAt.epochSecond - now.epochSecond)
}

data class FocusStats(
    val todayMinutes: Int,
    val todaySessions: Int,
    val weekMinutes: Int,
    /** Minutes for the last 7 days, oldest first, the last entry being today. */
    val last7Days: List<Int>,
)

/** Only sessions that ran at least one minute count; stopping a timer early still counts what was done. */
fun computeFocusStats(sessions: List<FocusSession>, today: LocalDate, zone: ZoneId): FocusStats {
    val byDay = sessions.groupBy { it.date(zone) }
    fun minutes(d: LocalDate) = byDay[d].orEmpty().sumOf { it.actualMinutes }
    val days = (6 downTo 0).map { today.minusDays(it.toLong()) }
    val perDay = days.map(::minutes)
    return FocusStats(
        todayMinutes = minutes(today),
        todaySessions = byDay[today].orEmpty().count { it.actualMinutes >= 1 },
        weekMinutes = perDay.sum(),
        last7Days = perDay,
    )
}
