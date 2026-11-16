package com.roozyar.app.feature.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.roozyar.app.core.common.toPersianDigits
import com.roozyar.app.core.date.JalaliFormatter
import com.roozyar.app.core.date.toJalali
import com.roozyar.app.core.design.RoozYarCard
import com.roozyar.app.core.design.RoozYarTopBar
import com.roozyar.app.domain.model.Event
import com.roozyar.app.feature.habits.habitIcon
import com.roozyar.app.feature.tasks.TaskItem
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale

private val weekdayHeaders = listOf("ش", "ی", "د", "س", "چ", "پ", "ج")

@Composable
fun CalendarScreen(
    onAddEvent: (LocalDate) -> Unit,
    onEditEvent: (Long) -> Unit,
    onEditTask: (Long) -> Unit,
    onOpenGoal: (Long) -> Unit,
    onOpenHabits: () -> Unit,
    viewModel: CalendarViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val scheme = MaterialTheme.colorScheme

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            RoozYarTopBar(
                title = "تقویم شمسی",
                actions = { AssistChip(onClick = viewModel::goToToday, label = { Text("امروز") }, modifier = Modifier.padding(end = 8.dp)) },
            )
            when {
                state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                state.error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(state.error!!, color = scheme.error) }
                else -> Column(
                    Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(start = 16.dp, end = 16.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    MonthHeader(state, viewModel::previousMonth, viewModel::nextMonth)
                    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CalendarFilter.entries.forEach { f ->
                            FilterChip(
                                selected = state.filter == f,
                                onClick = { viewModel.onFilterChange(f) },
                                label = { Text(f.label) },
                                shape = RoundedCornerShape(50),
                            )
                        }
                    }
                    MonthGrid(state, viewModel::selectDate)
                    Legend()
                    Agenda(state, viewModel, onEditEvent, onEditTask, onOpenGoal, onOpenHabits)
                }
            }
        }
        ExtendedFloatingActionButton(
            onClick = { onAddEvent(state.selected) },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
            containerColor = scheme.primaryContainer,
            contentColor = scheme.onPrimaryContainer,
            shape = RoundedCornerShape(50),
            icon = { Icon(Icons.Filled.Add, contentDescription = null) },
            text = { Text("رویداد جدید", style = MaterialTheme.typography.labelLarge) },
        )
    }
}

@Composable
private fun MonthHeader(state: CalendarUiState, onPrevious: () -> Unit, onNext: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onPrevious) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "ماه قبل") }
        Text(
            "${JalaliFormatter.monthName(state.month)} ${state.year.toPersianDigits()}",
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleLarge,
        )
        IconButton(onClick = onNext) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "ماه بعد") }
    }
}

@Composable
private fun MonthGrid(state: CalendarUiState, onSelect: (LocalDate) -> Unit) {
    val scheme = MaterialTheme.colorScheme
    RoozYarCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(8.dp)) {
            Row(Modifier.fillMaxWidth()) {
                weekdayHeaders.forEachIndexed { i, h ->
                    Text(
                        h,
                        modifier = Modifier.weight(1f).padding(vertical = 8.dp),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (i == 6) scheme.error else scheme.onSurfaceVariant,
                    )
                }
            }
            state.grid.forEach { week ->
                Row(Modifier.fillMaxWidth()) {
                    week.forEachIndexed { i, jd ->
                        if (jd == null) {
                            Box(Modifier.weight(1f).aspectRatio(1f))
                        } else {
                            val date = jd.toLocalDate()
                            DayCell(
                                day = jd.day,
                                isFriday = i == 6,
                                isSelected = date == state.selected,
                                isToday = date == state.today,
                                markers = state.markers[date].orEmpty(),
                                modifier = Modifier.weight(1f),
                                onClick = { onSelect(date) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(day: Int, isFriday: Boolean, isSelected: Boolean, isToday: Boolean, markers: Set<Marker>, modifier: Modifier, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(CircleShape)
            .background(if (isSelected) scheme.primaryContainer else Color.Transparent)
            .then(if (isToday && !isSelected) Modifier.border(1.dp, scheme.primary, CircleShape) else Modifier)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                day.toPersianDigits(),
                style = MaterialTheme.typography.bodyMedium,
                color = when {
                    isSelected -> scheme.onPrimaryContainer
                    isFriday -> scheme.error
                    else -> scheme.onSurface
                },
            )
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.padding(top = 1.dp)) {
                if (markers.isEmpty()) {
                    Box(Modifier.size(5.dp))
                } else {
                    markers.sortedBy { it.ordinal }.forEach { m ->
                        Box(Modifier.size(5.dp).clip(CircleShape).background(markerColor(m)))
                    }
                }
            }
        }
    }
}

@Composable
private fun markerColor(marker: Marker): Color = when (marker) {
    Marker.EVENT -> MaterialTheme.colorScheme.primary
    Marker.TASK -> MaterialTheme.colorScheme.secondary
    Marker.GOAL -> MaterialTheme.colorScheme.tertiary
}

@Composable
private fun Legend() {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)) {
        listOf(Marker.EVENT to "رویداد", Marker.TASK to "وظیفه", Marker.GOAL to "مهلت هدف").forEach { (m, label) ->
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(Modifier.size(8.dp).clip(CircleShape).background(markerColor(m)))
                Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun Agenda(
    state: CalendarUiState,
    viewModel: CalendarViewModel,
    onEditEvent: (Long) -> Unit,
    onEditTask: (Long) -> Unit,
    onOpenGoal: (Long) -> Unit,
    onOpenHabits: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val agenda = state.agenda
    val j = agenda.date.toJalali()
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            "برنامه‌های ${JalaliFormatter.weekdayName(agenda.date.dayOfWeek)} ${j.day.toPersianDigits()} ${JalaliFormatter.monthName(j.month)}",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f),
        )
        Text("${agenda.itemCount.toPersianDigits()} مورد", style = MaterialTheme.typography.labelMedium, color = scheme.onSurfaceVariant)
    }
    if (agenda.itemCount == 0 && agenda.habits.isEmpty()) {
        Text("برای این روز برنامه‌ای ثبت نشده است.", style = MaterialTheme.typography.bodyMedium, color = scheme.onSurfaceVariant)
    }
    agenda.events.forEach { event -> EventCard(event, state.zone) { onEditEvent(event.id) } }
    agenda.tasks.forEach { task ->
        TaskItem(
            task = task,
            now = state.now,
            zone = state.zone,
            onToggle = { viewModel.toggleTask(task) },
            onClick = { onEditTask(task.id) },
        )
    }
    agenda.goals.forEach { goal ->
        RoozYarCard(Modifier.fillMaxWidth(), onClick = { onOpenGoal(goal.id) }) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("مهلت هدف", style = MaterialTheme.typography.labelSmall, color = scheme.tertiary)
                Text(goal.title, style = MaterialTheme.typography.titleSmall)
            }
        }
    }
    if (agenda.habits.isNotEmpty()) {
        Text("عادت‌ها", style = MaterialTheme.typography.titleSmall, color = scheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
        agenda.habits.forEach { habit ->
            val done = habit.id in state.habitsDone
            RoozYarCard(Modifier.fillMaxWidth(), onClick = onOpenHabits) {
                Row(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(habitIcon(habit.icon), contentDescription = null, tint = scheme.primary, modifier = Modifier.size(22.dp))
                    Text(habit.title, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                    Icon(
                        if (done) Icons.Filled.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                        contentDescription = if (done) "انجام شده" else "انجام نشده",
                        tint = if (done) scheme.primary else scheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun EventCard(event: Event, zone: ZoneId, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    RoozYarCard(Modifier.fillMaxWidth(), onClick = onClick) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(event.title, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                Surface(shape = MaterialTheme.shapes.small, color = scheme.primary.copy(alpha = 0.15f)) {
                    Text(
                        event.category.label,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = scheme.primary,
                    )
                }
            }
            Text(
                formatEventTime(event, zone) + if (event.location.isNotBlank()) " • ${event.location}" else "",
                style = MaterialTheme.typography.labelMedium,
                color = scheme.onSurfaceVariant,
            )
        }
    }
}

/** «۱۰:۳۰ تا ۱۲:۰۰», «۱۰:۳۰» or «تمام روز». */
fun formatEventTime(event: Event, zone: ZoneId): String {
    if (!event.hasTime) return "تمام روز"
    fun hm(i: java.time.Instant): String {
        val t = i.atZone(zone)
        return String.format(Locale.ROOT, "%02d:%02d", t.hour, t.minute).toPersianDigits()
    }
    val end = event.end
    return if (end != null) "${hm(event.start)} تا ${hm(end)}" else hm(event.start)
}
