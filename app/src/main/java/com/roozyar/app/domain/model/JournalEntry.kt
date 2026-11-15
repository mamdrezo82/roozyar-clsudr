package com.roozyar.app.domain.model

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

enum class JournalMood(val label: String, val emoji: String) {
    GREAT("عالی", "😄"),
    GOOD("خوب", "🙂"),
    OKAY("معمولی", "😐"),
    TIRED("خسته", "😴"),
    SAD("ناراحت", "😔"),
    ENERGETIC("پرانرژی", "⚡");

    companion object {
        fun fromName(name: String): JournalMood? = entries.firstOrNull { it.name == name }
    }
}

data class JournalEntry(
    val id: Long,
    val title: String,
    val body: String,
    val mood: JournalMood?,
    val tags: List<String>,
    val starred: Boolean,
    val isDraft: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    val wordCount: Int get() = countWords(body)

    /** Rough reading time at 200 words per minute, at least one minute. */
    val readingMinutes: Int get() = maxOf(1, (wordCount + 199) / 200)

    /** Title, or the first line of the body when the title is blank. */
    val displayTitle: String get() = title.ifBlank { body.lineSequence().firstOrNull { it.isNotBlank() }.orEmpty().take(60) }
}

fun countWords(text: String): Int = text.trim().split(Regex("\\s+")).count { it.isNotEmpty() }

/** Splits on commas (Latin and Persian) and newlines, strips a leading '#', trims, and removes blanks and duplicates. */
fun parseTags(input: String): List<String> =
    input.split(',', '،', '\n')
        .map { it.trim().removePrefix("#").trim() }
        .filter { it.isNotEmpty() }
        .distinct()

/** Consecutive days with a published entry, ending today or yesterday (an unwritten today does not break it). */
fun journalStreak(entryDays: Set<LocalDate>, today: LocalDate): Int {
    var day = if (today in entryDays) today else today.minusDays(1)
    var streak = 0
    while (day in entryDays) {
        streak++
        day = day.minusDays(1)
    }
    return streak
}

fun JournalEntry.localDate(zone: ZoneId): LocalDate = createdAt.atZone(zone).toLocalDate()
