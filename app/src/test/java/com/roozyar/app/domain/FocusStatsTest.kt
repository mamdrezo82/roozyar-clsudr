package com.roozyar.app.domain

import com.roozyar.app.domain.model.ActiveFocus
import com.roozyar.app.domain.model.FocusKind
import com.roozyar.app.domain.model.FocusSession
import com.roozyar.app.domain.model.computeFocusStats
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

class FocusStatsTest {
    private val today = LocalDate.of(2026, 11, 11)
    private fun session(daysBack: Int, minutes: Int, completed: Boolean = true) = FocusSession(
        0, today.minusDays(daysBack.toLong()).atTime(10, 0).toInstant(ZoneOffset.UTC), 25, minutes * 60, completed, null, null,
    )

    @Test
    fun statsSplitTodayAndWeek() {
        val s = computeFocusStats(listOf(session(0, 25), session(0, 10, false), session(2, 30), session(9, 99)), today, ZoneOffset.UTC)
        assertEquals(35, s.todayMinutes)
        assertEquals(2, s.todaySessions)
        assertEquals(65, s.weekMinutes) // the 9-days-old session is outside the window
        assertEquals(7, s.last7Days.size)
        assertEquals(35, s.last7Days.last())
        assertEquals(30, s.last7Days[4])
    }

    @Test
    fun sessionsUnderAMinuteDoNotCountAsSessions() {
        val tiny = FocusSession(0, today.atTime(9, 0).toInstant(ZoneOffset.UTC), 25, 30, false, null, null)
        val s = computeFocusStats(listOf(tiny), today, ZoneOffset.UTC)
        assertEquals(0, s.todaySessions)
        assertEquals(0, s.todayMinutes)
    }

    @Test
    fun remainingSecondsNeverGoesNegative() {
        val start = Instant.parse("2026-11-11T10:00:00Z")
        val a = ActiveFocus(FocusKind.WORK, start, start.plusSeconds(600), 10, null, null)
        assertEquals(300, a.remainingSeconds(start.plusSeconds(300)))
        assertEquals(0, a.remainingSeconds(start.plusSeconds(900)))
    }
}
