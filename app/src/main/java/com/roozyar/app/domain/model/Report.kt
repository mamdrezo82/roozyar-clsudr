package com.roozyar.app.domain.model

import java.time.LocalDate
import java.time.ZoneId

enum class ReportPeriod(val label: String, val days: Int) {
    DAY("امروز", 1),
    WEEK("هفته", 7),
    MONTH("ماه", 30),
}

data class PeriodMetrics(
    val tasksDone: Int,
    val focusMinutes: Int,
    val journalEntries: Int,
    val habitDone: Int,
    val habitScheduled: Int,
) {
    /** 0..100, or null when no habit was scheduled in the period. */
    val habitAdherence: Int? get() = if (habitScheduled == 0) null else habitDone * 100 / habitScheduled
    val isEmpty: Boolean get() = tasksDone == 0 && focusMinutes == 0 && journalEntries == 0 && habitDone == 0
}

data class HabitAdherence(val title: String, val percent: Int)

data class PeriodReport(
    val period: ReportPeriod,
    val current: PeriodMetrics,
    val previous: PeriodMetrics,
    val habits: List<HabitAdherence>,
    val insights: List<String>,
)

private fun metricsFor(
    start: LocalDate,
    end: LocalDate,
    tasks: List<Task>,
    habits: List<Habit>,
    logs: List<HabitLog>,
    sessions: List<FocusSession>,
    journal: List<JournalEntry>,
    zone: ZoneId,
): Pair<PeriodMetrics, List<HabitAdherence>> {
    fun inRange(d: LocalDate) = !d.isBefore(start) && !d.isAfter(end)
    val byHabit = logs.groupBy { it.habitId }
    var done = 0
    var scheduled = 0
    val perHabit = mutableListOf<HabitAdherence>()
    habits.forEach { h ->
        val counts = byHabit[h.id].orEmpty().associate { it.date to it.count }
        val created = h.createdAt.atZone(zone).toLocalDate()
        var hs = 0
        var hd = 0
        var d = if (start.isBefore(created)) created else start
        while (!d.isAfter(end)) {
            if (h.isScheduledOn(d)) {
                hs++
                if ((counts[d] ?: 0) >= h.targetCount) hd++
            }
            d = d.plusDays(1)
        }
        scheduled += hs
        done += hd
        if (hs > 0) perHabit += HabitAdherence(h.title, hd * 100 / hs)
    }
    return PeriodMetrics(
        tasksDone = tasks.count { it.status == TaskStatus.DONE && inRange(it.updatedAt.atZone(zone).toLocalDate()) },
        focusMinutes = sessions.filter { inRange(it.date(zone)) }.sumOf { it.actualMinutes },
        journalEntries = journal.count { !it.isDraft && inRange(it.localDate(zone)) },
        habitDone = done,
        habitScheduled = scheduled,
    ) to perHabit
}

/** Report for the [period] ending [today], compared with the period of the same length right before it. */
fun buildReport(
    period: ReportPeriod,
    tasks: List<Task>,
    habits: List<Habit>,
    logs: List<HabitLog>,
    sessions: List<FocusSession>,
    journal: List<JournalEntry>,
    today: LocalDate,
    zone: ZoneId,
): PeriodReport {
    val len = period.days.toLong()
    val curStart = today.minusDays(len - 1)
    val prevEnd = curStart.minusDays(1)
    val prevStart = prevEnd.minusDays(len - 1)
    val (current, perHabit) = metricsFor(curStart, today, tasks, habits, logs, sessions, journal, zone)
    val (previous, _) = metricsFor(prevStart, prevEnd, tasks, habits, logs, sessions, journal, zone)

    val insights = mutableListOf<String>()
    if (current.isEmpty && current.habitScheduled == 0) {
        insights += "در این بازه فعالیتی ثبت نشده است."
    } else {
        val taskDiff = current.tasksDone - previous.tasksDone
        if (taskDiff > 0) insights += "$taskDiff وظیفه بیشتر از دوره قبل انجام دادید."
        if (taskDiff < 0 && previous.tasksDone > 0) insights += "${-taskDiff} وظیفه کمتر از دوره قبل انجام دادید."
        val focusDiff = current.focusMinutes - previous.focusMinutes
        if (focusDiff > 0) insights += "$focusDiff دقیقه بیشتر از دوره قبل تمرکز کردید."
        if (focusDiff < 0 && previous.focusMinutes > 0) insights += "${-focusDiff} دقیقه کمتر از دوره قبل تمرکز کردید."
        perHabit.maxByOrNull { it.percent }?.let { if (it.percent > 0) insights += "بهترین پایبندی: «${it.title}» با ٪${it.percent}." }
        perHabit.minByOrNull { it.percent }?.let { if (it.percent < 50) insights += "«${it.title}» نیاز به توجه بیشتری دارد (٪${it.percent})." }
    }
    return PeriodReport(period, current, previous, perHabit.sortedByDescending { it.percent }, insights.take(4))
}
