package com.roozyar.app.core.date

import java.time.DayOfWeek
import java.time.LocalDate

/** A validated date in the Jalali (Persian) calendar. */
data class JalaliDate(val year: Int, val month: Int, val day: Int) : Comparable<JalaliDate> {
    init {
        require(year in JalaliCalendarMath.MIN_YEAR until JalaliCalendarMath.MAX_YEAR_EXCLUSIVE) { "Invalid Jalali year $year" }
        require(month in 1..12) { "Invalid Jalali month $month" }
        require(day in 1..monthLength(year, month)) { "Invalid Jalali day $day for $year/$month" }
    }

    fun toLocalDate(): LocalDate {
        val g = JalaliCalendarMath.jalaliToGregorian(year, month, day)
        return LocalDate.of(g.year, g.month, g.day)
    }

    val dayOfWeek: DayOfWeek get() = toLocalDate().dayOfWeek

    override fun compareTo(other: JalaliDate): Int =
        compareValuesBy(this, other, JalaliDate::year, JalaliDate::month, JalaliDate::day)

    companion object {
        fun from(date: LocalDate): JalaliDate {
            val j = JalaliCalendarMath.gregorianToJalali(date.year, date.monthValue, date.dayOfMonth)
            return JalaliDate(j.year, j.month, j.day)
        }

        fun isLeapYear(year: Int): Boolean = JalaliCalendarMath.isLeap(year)

        fun monthLength(year: Int, month: Int): Int = when {
            month <= 6 -> 31
            month <= 11 -> 30
            else -> if (isLeapYear(year)) 30 else 29
        }
    }
}

fun LocalDate.toJalali(): JalaliDate = JalaliDate.from(this)
