package com.roozyar.app.feature

import com.roozyar.app.domain.model.TimelineItem
import com.roozyar.app.domain.model.TimelineType
import com.roozyar.app.feature.timeline.groupByDay
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

class TimelineGroupTest {
    @Test
    fun groupsByLocalDayNewestDayFirst() {
        val items = listOf(
            TimelineItem(TimelineType.TASK, Instant.parse("2026-11-11T20:00:00Z"), "a"),
            TimelineItem(TimelineType.TASK, Instant.parse("2026-11-11T08:00:00Z"), "b"),
            TimelineItem(TimelineType.TASK, Instant.parse("2026-11-09T08:00:00Z"), "c"),
        )
        val days = groupByDay(items, ZoneOffset.UTC)
        assertEquals(listOf(LocalDate.of(2026, 11, 11), LocalDate.of(2026, 11, 9)), days.map { it.first })
        assertEquals(listOf("a", "b"), days.first().second.map { it.title })
    }

    @Test
    fun timeZoneMovesLateEveningItemsToTheNextDay() {
        val items = listOf(TimelineItem(TimelineType.TASK, Instant.parse("2026-11-10T21:00:00Z"), "late"))
        assertEquals(LocalDate.of(2026, 11, 11), groupByDay(items, ZoneOffset.ofHours(4)).first().first)
    }
}
