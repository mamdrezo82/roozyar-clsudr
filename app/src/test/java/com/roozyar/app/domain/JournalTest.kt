package com.roozyar.app.domain

import com.roozyar.app.domain.model.JournalEntry
import com.roozyar.app.domain.model.JournalMood
import com.roozyar.app.domain.model.countWords
import com.roozyar.app.domain.model.journalStreak
import com.roozyar.app.domain.model.parseTags
import com.roozyar.app.feature.journal.JournalFilter
import com.roozyar.app.feature.journal.computeStats
import com.roozyar.app.feature.journal.filterEntries
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

class JournalTest {
    private val today = LocalDate.of(2026, 11, 11)
    private val zone = ZoneOffset.UTC

    private fun entry(
        id: Long = 1,
        title: String = "t",
        body: String = "b",
        daysBack: Int = 0,
        draft: Boolean = false,
        starred: Boolean = false,
        tags: List<String> = emptyList(),
        mood: JournalMood? = null,
    ): JournalEntry {
        val at = today.minusDays(daysBack.toLong()).atTime(12, 0).toInstant(ZoneOffset.UTC)
        return JournalEntry(id, title, body, mood, tags, starred, draft, at, at)
    }

    @Test
    fun parsesTagsFromMixedSeparators() {
        assertEquals(listOf("کار", "ایده", "work"), parseTags("#کار، ایده, work ,, کار"))
        assertEquals(emptyList<String>(), parseTags("  ، , "))
    }

    @Test
    fun countsWordsAndEstimatesReading() {
        assertEquals(0, countWords("   "))
        assertEquals(3, countWords("سلام  به\nدنیا"))
        assertEquals(1, entry(body = "a b c").readingMinutes)
        assertEquals(2, entry(body = List(201) { "w" }.joinToString(" ")).readingMinutes)
    }

    @Test
    fun displayTitleFallsBackToFirstBodyLine() {
        assertEquals("خط اول", entry(title = "", body = "\nخط اول\nخط دوم").displayTitle)
        assertEquals("عنوان", entry(title = "عنوان").displayTitle)
    }

    @Test
    fun streakEndsTodayOrYesterday() {
        val days = setOf(today, today.minusDays(1), today.minusDays(2), today.minusDays(4))
        assertEquals(3, journalStreak(days, today))
        assertEquals(2, journalStreak(setOf(today.minusDays(1), today.minusDays(2)), today))
        assertEquals(0, journalStreak(setOf(today.minusDays(2)), today))
    }

    @Test
    fun draftsAreExcludedFromStats() {
        val entries = listOf(
            entry(id = 1, body = "a b c", daysBack = 0),
            entry(id = 2, body = "d e", daysBack = 1),
            entry(id = 3, body = "x y z w", daysBack = 0, draft = true),
            entry(id = 4, body = "old old", daysBack = 20),
        )
        val s = computeStats(entries, today, zone)
        assertEquals(2, s.streakDays)
        assertEquals(3, s.entryCount)
        assertEquals(5, s.wordsThisWeek)
    }

    @Test
    fun filtersAndSearches() {
        val entries = listOf(
            entry(id = 1, title = "روز خوب", starred = true),
            entry(id = 2, body = "جلسه", draft = true, tags = listOf("کار")),
            entry(id = 3, title = "other"),
        )
        assertEquals(listOf(1L), filterEntries(entries, "", JournalFilter.STARRED).map { it.id })
        assertEquals(listOf(2L), filterEntries(entries, "", JournalFilter.DRAFTS).map { it.id })
        assertEquals(listOf(2L), filterEntries(entries, "کار", JournalFilter.ALL).map { it.id })
        assertEquals(listOf(3L), filterEntries(entries, "OTHER", JournalFilter.ALL).map { it.id })
    }
}
