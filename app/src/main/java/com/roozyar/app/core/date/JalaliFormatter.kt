package com.roozyar.app.core.date

import com.roozyar.app.core.common.toPersianDigits
import java.time.DayOfWeek

object JalaliFormatter {
    private val monthNames = listOf(
        "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند",
    )

    fun monthName(month: Int): String = monthNames[month - 1]

    fun weekdayName(day: DayOfWeek): String = when (day) {
        DayOfWeek.SATURDAY -> "شنبه"
        DayOfWeek.SUNDAY -> "یکشنبه"
        DayOfWeek.MONDAY -> "دوشنبه"
        DayOfWeek.TUESDAY -> "سه‌شنبه"
        DayOfWeek.WEDNESDAY -> "چهارشنبه"
        DayOfWeek.THURSDAY -> "پنجشنبه"
        DayOfWeek.FRIDAY -> "جمعه"
    }

    /** e.g. «چهارشنبه، ۲۰ آبان ۱۴۰۵» */
    fun full(date: JalaliDate): String =
        "${weekdayName(date.dayOfWeek)}، ${date.day.toPersianDigits()} ${monthName(date.month)} ${date.year.toPersianDigits()}"
}
