package com.roozyar.app.domain.model

import java.time.Instant

enum class TimelineType(val label: String) {
    TASK("وظیفه"),
    GOAL("هدف"),
    JOURNAL("ژورنال"),
    FOCUS("تمرکز"),
    ACHIEVEMENT("دستاورد"),
    EVENT("رویداد"),
}

data class TimelineItem(val type: TimelineType, val at: Instant, val title: String, val subtitle: String? = null)

/**
 * A newest-first history built only from stored data. Completed tasks are dated by their last update, so editing
 * a finished task moves it on the timeline. Drafts, focus sessions under a minute and future events are left out.
 */
fun buildTimeline(
    tasks: List<Task>,
    goals: List<Goal>,
    journal: List<JournalEntry>,
    sessions: List<FocusSession>,
    unlocked: Map<String, Instant>,
    events: List<Event>,
    now: Instant,
): List<TimelineItem> {
    val items = mutableListOf<TimelineItem>()
    tasks.filter { it.status == TaskStatus.DONE }.forEach { items += TimelineItem(TimelineType.TASK, it.updatedAt, "وظیفه انجام شد", it.title) }
    goals.forEach { g ->
        items += TimelineItem(TimelineType.GOAL, g.createdAt, "هدف تعریف شد", g.title)
        val done = g.completedAt
        if (g.completed && done != null) items += TimelineItem(TimelineType.GOAL, done, "هدف تکمیل شد", g.title)
    }
    journal.filter { !it.isDraft }.forEach { items += TimelineItem(TimelineType.JOURNAL, it.createdAt, "یادداشت", it.displayTitle.ifBlank { null }) }
    sessions.filter { it.actualMinutes >= 1 }.forEach { items += TimelineItem(TimelineType.FOCUS, it.startedAt, "جلسه تمرکز", "${it.actualMinutes} دقیقه") }
    val titles = AchievementCatalog.all.associate { it.code to it.title }
    unlocked.forEach { (code, at) -> titles[code]?.let { items += TimelineItem(TimelineType.ACHIEVEMENT, at, "دستاورد جدید", it) } }
    events.filter { !it.start.isAfter(now) }.forEach { items += TimelineItem(TimelineType.EVENT, it.start, "رویداد", it.title) }
    return items.sortedByDescending { it.at }
}
