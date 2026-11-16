package com.roozyar.app.domain.model

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

enum class EventCategory(val label: String) {
    WORK("کاری"),
    PERSONAL("شخصی"),
    STUDY("مطالعه"),
    HEALTH("سلامت");

    companion object {
        fun fromName(name: String): EventCategory = entries.firstOrNull { it.name == name } ?: PERSONAL
    }
}

/** [hasTime] false means an all-day event; [end] is only meaningful when [hasTime] is true. */
data class Event(
    val id: Long,
    val title: String,
    val start: Instant,
    val end: Instant?,
    val hasTime: Boolean,
    val category: EventCategory,
    val location: String,
) {
    fun date(zone: ZoneId): LocalDate = start.atZone(zone).toLocalDate()
}
