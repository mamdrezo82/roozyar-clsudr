package com.roozyar.app.domain

import com.roozyar.app.domain.model.DashboardCard
import com.roozyar.app.domain.model.DashboardLayout
import com.roozyar.app.domain.model.DashboardMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class DashboardLayoutTest {
    @Test
    fun everyPresetListsEveryCardOnce() {
        DashboardMode.entries.forEach { m ->
            val l = DashboardLayout.preset(m)
            assertEquals(DashboardCard.entries.toSet(), l.order.toSet())
            assertEquals(DashboardCard.entries.size, l.order.size)
            assertTrue(l.visible.isNotEmpty())
        }
    }

    @Test
    fun presetsLeadWithTheirFocus() {
        assertEquals(DashboardCard.FOCUS, DashboardLayout.preset(DashboardMode.EXAM).visible.first())
        assertEquals(DashboardCard.EVENTS, DashboardLayout.preset(DashboardMode.INTERNSHIP).visible.first())
        assertEquals(DashboardCard.PROGRESS, DashboardLayout.preset(DashboardMode.GENERAL).visible.first())
    }

    @Test
    fun toggleAndMove() {
        val base = DashboardLayout.preset(DashboardMode.GENERAL)
        val hiddenProgress = base.toggled(DashboardCard.PROGRESS)
        assertTrue(DashboardCard.PROGRESS !in hiddenProgress.visible)
        assertTrue(DashboardCard.PROGRESS in hiddenProgress.toggled(DashboardCard.PROGRESS).visible)

        val moved = base.moved(DashboardCard.PROGRESS, 1)
        assertEquals(1, moved.order.indexOf(DashboardCard.PROGRESS))
        assertSame(base, base.moved(DashboardCard.PROGRESS, -1)) // already first
        assertSame(base, base.moved(base.order.last(), 1)) // already last
    }

    @Test
    fun parseRoundTrips() {
        val custom = DashboardLayout.preset(DashboardMode.EXAM).moved(DashboardCard.GOALS, -1).toggled(DashboardCard.HABITS)
        val back = DashboardLayout.parse(custom.mode.name, custom.serializedOrder(), custom.serializedHidden())
        assertEquals(custom, back)
    }

    @Test
    fun parseIsTolerant() {
        assertEquals(DashboardLayout.preset(DashboardMode.GENERAL), DashboardLayout.parse(null, null, null))
        val l = DashboardLayout.parse("EXAM", "GOALS,NOPE,GOALS,FOCUS", "FOCUS,BOGUS")
        assertEquals(listOf(DashboardCard.GOALS, DashboardCard.FOCUS), l.order.take(2))
        assertEquals(DashboardCard.entries.size, l.order.size) // missing cards appended
        assertTrue(DashboardCard.FOCUS in l.hidden)
        assertTrue(DashboardCard.PROGRESS in l.hidden) // appended cards start hidden
        assertEquals(DashboardMode.GENERAL, DashboardLayout.parse("junk", null, null).mode)
    }
}
