package com.roozyar.app.domain

import com.roozyar.app.data.repository.weekdaysFromMask
import com.roozyar.app.data.repository.weekdaysToMask
import com.roozyar.app.domain.model.Habit
import com.roozyar.app.domain.model.completionPercent
import com.roozyar.app.domain.model.currentStreak
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate

class HabitStatsTest {
    private val today = LocalDate.of(2026, 11, 11) // Wednesday
    private val created = LocalDate.of(2026, 1, 1)

    private fun habit(target: Int = 1, days: Set<DayOfWeek> = DayOfWeek.entries.toSet()) =
        Habit(1, "h", "star", days, target, "", Instant.EPOCH)

    private fun counts(vararg daysBack: Int, value: Int = 1) = daysBack.associate { today.minusDays(it.toLong()) to value }

    @Test
    fun streakCountsConsecutiveDaysIncludingToday() {
        assertEquals(3, currentStreak(habit(), counts(0, 1, 2), today, created))
    }

    @Test
    fun openTodayDoesNotBreakTheStreak() {
        assertEquals(2, currentStreak(habit(), counts(1, 2), today, created))
    }

    @Test
    fun missedDayBreaksTheStreak() {
        assertEquals(1, currentStreak(habit(), counts(0, 2, 3), today, created))
    }

    @Test
    fun unscheduledDaysAreSkipped() {
        val monWedFri = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)
        // Wed 11, Mon 9, Fri 6 done; Tue 10 / Thu 8 / Sat 7 are not scheduled
        assertEquals(3, currentStreak(habit(days = monWedFri), counts(0, 2, 5), today, created))
    }

    @Test
    fun targetMustBeReached() {
        assertEquals(0, currentStreak(habit(target = 8), counts(0, 1, value = 5), today, created))
        assertEquals(2, currentStreak(habit(target = 8), counts(0, 1, value = 8), today, created))
    }

    @Test
    fun streakNeverGoesBeforeCreation() {
        assertEquals(2, currentStreak(habit(), counts(0, 1, 2, 3), today, today.minusDays(1)))
    }

    @Test
    fun completionPercentOverScheduledDays() {
        // created 4 days ago -> window is 5 days (today included); 2 of 5 done
        assertEquals(40, completionPercent(habit(), counts(0, 2), today, today.minusDays(4)))
    }

    @Test
    fun completionIsNullWhenNothingWasScheduled() {
        assertNull(completionPercent(habit(days = setOf(DayOfWeek.SUNDAY)), emptyMap(), today, today))
    }

    @Test
    fun weekdayMaskRoundTrips() {
        val days = setOf(DayOfWeek.MONDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
        assertEquals(days, weekdaysFromMask(weekdaysToMask(days)))
        assertEquals(127, weekdaysToMask(DayOfWeek.entries.toSet()))
    }
}
