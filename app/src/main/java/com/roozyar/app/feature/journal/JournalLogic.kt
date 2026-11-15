package com.roozyar.app.feature.journal

import com.roozyar.app.core.common.toPersianDigits
import com.roozyar.app.core.date.JalaliFormatter
import com.roozyar.app.core.date.toJalali
import com.roozyar.app.domain.model.JournalEntry
import com.roozyar.app.domain.model.journalStreak
import com.roozyar.app.domain.model.localDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Locale

enum class JournalFilter(val label: String) {
    ALL("همه"),
    STARRED("ستاره‌دار"),
    DRAFTS("پیش‌نویس"),
}

data class JournalStats(val streakDays: Int, val entryCount: Int, val wordsThisWeek: Int)

fun filterEntries(entries: List<JournalEntry>, query: String, filter: JournalFilter): List<JournalEntry> {
    val q = query.trim()
    return entries.filter { e ->
        when (filter) {
            JournalFilter.ALL -> true
            JournalFilter.STARRED -> e.starred
            JournalFilter.DRAFTS -> e.isDraft
        } && (
            q.isEmpty() ||
                e.title.contains(q, ignoreCase = true) ||
                e.body.contains(q, ignoreCase = true) ||
                e.tags.any { it.contains(q, ignoreCase = true) }
            )
    }
}

/** Drafts never count towards the streak, the entry total or the weekly word count. */
fun computeStats(entries: List<JournalEntry>, today: java.time.LocalDate, zone: ZoneId): JournalStats {
    val published = entries.filter { !it.isDraft }
    val weekStart = today.minusDays(6)
    return JournalStats(
        streakDays = journalStreak(published.map { it.localDate(zone) }.toSet(), today),
        entryCount = published.size,
        wordsThisWeek = published.filter { !it.localDate(zone).isBefore(weekStart) }.sumOf { it.wordCount },
    )
}

/** e.g. «۲۰ آبان ۱۴۰۵ • ۱۴:۳۰» */
fun formatEntryDate(entry: JournalEntry, zone: ZoneId): String {
    val local = LocalDateTime.ofInstant(entry.createdAt, zone)
    val j = local.toLocalDate().toJalali()
    val time = String.format(Locale.ROOT, "%02d:%02d", local.hour, local.minute)
    return "${j.day.toPersianDigits()} ${JalaliFormatter.monthName(j.month)} ${j.year.toPersianDigits()} • ${time.toPersianDigits()}"
}
