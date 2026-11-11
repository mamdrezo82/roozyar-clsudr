package com.roozyar.app.core.date

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate

class JalaliDateTest {

    private fun assertConverts(gregorian: LocalDate, jalali: JalaliDate) {
        assertEquals(jalali, gregorian.toJalali())
        assertEquals(gregorian, jalali.toLocalDate())
    }

    @Test
    fun knownNowruzDates() {
        assertConverts(LocalDate.of(2021, 3, 21), JalaliDate(1400, 1, 1))
        assertConverts(LocalDate.of(2024, 3, 20), JalaliDate(1403, 1, 1))
        assertConverts(LocalDate.of(2025, 3, 21), JalaliDate(1404, 1, 1))
        assertConverts(LocalDate.of(2026, 3, 21), JalaliDate(1405, 1, 1))
    }

    @Test
    fun lastDayOfLeapYears() {
        assertConverts(LocalDate.of(2021, 3, 20), JalaliDate(1399, 12, 30))
        assertConverts(LocalDate.of(2025, 3, 20), JalaliDate(1403, 12, 30))
    }

    @Test
    fun autumnDates() {
        assertConverts(LocalDate.of(2026, 9, 23), JalaliDate(1405, 7, 1))
        assertConverts(LocalDate.of(2026, 11, 11), JalaliDate(1405, 8, 20))
    }

    @Test
    fun leapYears() {
        listOf(1395, 1399, 1403, 1408).forEach { assertTrue("$it should be leap", JalaliDate.isLeapYear(it)) }
        listOf(1400, 1401, 1402, 1404, 1405).forEach { assertFalse("$it should not be leap", JalaliDate.isLeapYear(it)) }
    }

    @Test
    fun monthLengths() {
        assertEquals(31, JalaliDate.monthLength(1405, 1))
        assertEquals(31, JalaliDate.monthLength(1405, 6))
        assertEquals(30, JalaliDate.monthLength(1405, 7))
        assertEquals(30, JalaliDate.monthLength(1405, 11))
        assertEquals(29, JalaliDate.monthLength(1404, 12))
        assertEquals(30, JalaliDate.monthLength(1403, 12))
    }

    @Test
    fun weekdayOfKnownDate() {
        assertEquals(DayOfWeek.WEDNESDAY, JalaliDate(1405, 8, 20).dayOfWeek)
        assertEquals("چهارشنبه، ۲۰ آبان ۱۴۰۵", JalaliFormatter.full(JalaliDate(1405, 8, 20)))
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsDay31InSecondHalf() {
        JalaliDate(1405, 7, 31)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsEsfand30InNonLeapYear() {
        JalaliDate(1404, 12, 30)
    }

    @Test
    fun everyDayRoundTripsAndAdvancesByOne() {
        var date = LocalDate.of(1990, 1, 1)
        val end = LocalDate.of(2100, 12, 31)
        var previous: JalaliDate? = null
        while (!date.isAfter(end)) {
            val jalali = date.toJalali()
            assertEquals("round trip failed for $date", date, jalali.toLocalDate())
            previous?.let { assertTrue("$date did not advance after $it", jalali > it) }
            previous?.let { assertEquals("gap before $date", 1L, daysBetween(it, jalali)) }
            previous = jalali
            date = date.plusDays(1)
        }
    }

    private fun daysBetween(a: JalaliDate, b: JalaliDate): Long =
        b.toLocalDate().toEpochDay() - a.toLocalDate().toEpochDay()
}
