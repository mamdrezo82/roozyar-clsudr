package com.roozyar.app.feature.goals

import com.roozyar.app.core.common.toPersianDigits
import com.roozyar.app.core.date.JalaliFormatter
import com.roozyar.app.core.date.toJalali
import com.roozyar.app.domain.model.Goal
import java.time.LocalDate

fun formatDeadline(date: LocalDate): String {
    val j = date.toJalali()
    return "${j.day.toPersianDigits()} ${JalaliFormatter.monthName(j.month)} ${j.year.toPersianDigits()}"
}

/** «۴۰ روز مانده» / «امروز» / «۳ روز گذشته»; null without a deadline or once completed. */
fun deadlineStatus(goal: Goal, today: LocalDate): String? {
    if (goal.completed) return null
    val days = goal.daysRemaining(today) ?: return null
    return when {
        days > 0 -> "${days.toInt().toPersianDigits()} روز مانده"
        days == 0L -> "مهلت امروز است"
        else -> "${(-days).toInt().toPersianDigits()} روز گذشته"
    }
}
