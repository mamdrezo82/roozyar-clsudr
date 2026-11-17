package com.roozyar.app.feature.planner

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.roozyar.app.core.common.toPersianDigits
import com.roozyar.app.core.date.JalaliFormatter
import com.roozyar.app.core.date.toJalali
import com.roozyar.app.core.design.RoozYarCard
import com.roozyar.app.core.design.RoozYarTimePickerDialog
import com.roozyar.app.core.design.RoozYarTopBar
import com.roozyar.app.domain.model.DayLoad
import com.roozyar.app.domain.model.PlanBlock
import com.roozyar.app.domain.model.PlanBlockKind
import com.roozyar.app.feature.tasks.formatDue
import java.time.LocalTime
import java.util.Locale

private fun hm(minute: Int): String = String.format(Locale.ROOT, "%02d:%02d", minute / 60 % 24, minute % 60).toPersianDigits()

private fun duration(minutes: Int): String {
    val h = minutes / 60
    val m = minutes % 60
    return when {
        h == 0 -> "${m.toPersianDigits()} دقیقه"
        m == 0 -> "${h.toPersianDigits()} ساعت"
        else -> "${h.toPersianDigits()} ساعت و ${m.toPersianDigits()} دقیقه"
    }
}

@Composable
fun PlannerScreen(onBack: () -> Unit, onEditTask: (Long) -> Unit, viewModel: PlannerViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val scheme = MaterialTheme.colorScheme
    var pickStart by remember { mutableStateOf(false) }
    var pickEnd by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        RoozYarTopBar("برنامه‌ریز هوشمند", onBack = onBack)
        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            state.error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(state.error!!, color = scheme.error) }
            else -> Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = state.dayOffset == 0, onClick = { viewModel.selectDay(0) }, label = { Text("امروز") }, shape = RoundedCornerShape(50))
                    FilterChip(selected = state.dayOffset == 1, onClick = { viewModel.selectDay(1) }, label = { Text("فردا") }, shape = RoundedCornerShape(50))
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("بازه کاری:", style = MaterialTheme.typography.labelLarge)
                    AssistChip(onClick = { pickStart = true }, label = { Text("از ${hm(state.windowStart)}") }, leadingIcon = { Icon(Icons.Outlined.Schedule, null) })
                    AssistChip(onClick = { pickEnd = true }, label = { Text("تا ${hm(state.windowEnd)}") }, leadingIcon = { Icon(Icons.Outlined.Schedule, null) })
                }
                if (state.windowError) Text("بازه کاری باید دست‌کم یک ساعت باشد.", color = scheme.error, style = MaterialTheme.typography.bodySmall)

                state.plan?.let { plan ->
                    Text("برنامه پیشنهادی", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "${JalaliFormatter.weekdayName(plan.date.dayOfWeek)} ${plan.date.toJalali().day.toPersianDigits()} ${JalaliFormatter.monthName(plan.date.toJalali().month)} • " +
                            "زمان آزاد باقی‌مانده: ${duration(plan.freeMinutes)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = scheme.onSurfaceVariant,
                    )
                    if (plan.blocks.isEmpty()) {
                        Text("کاری برای چیدن در این روز نیست.", style = MaterialTheme.typography.bodyMedium, color = scheme.onSurfaceVariant)
                    }
                    plan.blocks.forEach { b -> BlockCard(b, onClick = { b.taskId?.let(onEditTask) }) }
                    if (plan.unscheduled.isNotEmpty()) {
                        Text("جا نشد (${plan.unscheduled.size.toPersianDigits()})", style = MaterialTheme.typography.titleSmall, color = scheme.error)
                        Text("زمان آزاد کافی نبود. بازه کاری را بزرگ‌تر کنید یا برآورد زمان را کوتاه‌تر.", style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant)
                        plan.unscheduled.forEach { t ->
                            RoozYarCard(Modifier.fillMaxWidth(), onClick = { onEditTask(t.id) }) {
                                Text(t.title, modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleSmall)
                            }
                        }
                    }
                }

                if (state.overdue.isNotEmpty()) {
                    Text("عقب‌افتاده‌ها (${state.overdue.size.toPersianDigits()})", style = MaterialTheme.typography.titleMedium, color = scheme.error)
                    state.overdue.forEach { t ->
                        RoozYarCard(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(t.title, style = MaterialTheme.typography.titleSmall)
                                formatDue(t, state.zone)?.let { Text("مهلت: $it", style = MaterialTheme.typography.labelMedium, color = scheme.error) }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(onClick = { viewModel.moveOverdue(t, 0) }, shape = RoundedCornerShape(50)) { Text("انتقال به امروز") }
                                    OutlinedButton(onClick = { viewModel.moveOverdue(t, 1) }, shape = RoundedCornerShape(50)) { Text("انتقال به فردا") }
                                }
                            }
                        }
                    }
                }

                if (state.suggestions.isNotEmpty()) {
                    Text("مرحله بعدی اهداف", style = MaterialTheme.typography.titleMedium)
                    state.suggestions.forEach { s ->
                        RoozYarCard(Modifier.fillMaxWidth()) {
                            Row(Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(s.milestoneTitle, style = MaterialTheme.typography.titleSmall)
                                    Text(s.goalTitle, style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant)
                                }
                                TextButton(onClick = { viewModel.addMilestoneTask(s) }) { Text("افزودن به وظایف") }
                            }
                        }
                    }
                }

                Text("نمای هفته", style = MaterialTheme.typography.titleMedium)
                state.week.forEach { d -> WeekRow(d, state.windowEnd - state.windowStart) }
            }
        }
    }

    if (pickStart) {
        RoozYarTimePickerDialog(
            initial = LocalTime.of(state.windowStart / 60, state.windowStart % 60),
            onConfirm = { viewModel.setWindow(it.hour * 60 + it.minute, state.windowEnd); pickStart = false },
            onDismiss = { pickStart = false },
        )
    }
    if (pickEnd) {
        RoozYarTimePickerDialog(
            initial = LocalTime.of(state.windowEnd / 60 % 24, state.windowEnd % 60),
            onConfirm = { viewModel.setWindow(state.windowStart, it.hour * 60 + it.minute); pickEnd = false },
            onDismiss = { pickEnd = false },
        )
    }
}

@Composable
private fun BlockCard(b: PlanBlock, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val (label, color) = when (b.kind) {
        PlanBlockKind.EVENT -> "رویداد" to scheme.primary
        PlanBlockKind.FIXED_TASK -> "وظیفه با ساعت ثابت" to scheme.secondary
        PlanBlockKind.PLANNED_TASK -> "پیشنهاد" to scheme.tertiary
    }
    RoozYarCard(Modifier.fillMaxWidth(), onClick = if (b.taskId != null) onClick else null) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(hm(b.startMinute), style = MaterialTheme.typography.labelLarge, color = color)
                Text(hm(b.endMinute), style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant)
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(b.title, style = MaterialTheme.typography.titleSmall)
                Text(label, style = MaterialTheme.typography.labelSmall, color = color)
            }
        }
    }
}

@Composable
private fun WeekRow(d: DayLoad, windowMinutes: Int) {
    val scheme = MaterialTheme.colorScheme
    val j = d.date.toJalali()
    val overloaded = d.taskMinutes > windowMinutes
    RoozYarCard(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                "${JalaliFormatter.weekdayName(d.date.dayOfWeek)} ${j.day.toPersianDigits()} ${JalaliFormatter.monthName(j.month)}",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.weight(1f),
            )
            Text(
                "${d.taskCount.toPersianDigits()} وظیفه (${duration(d.taskMinutes)}) • ${d.eventCount.toPersianDigits()} رویداد",
                style = MaterialTheme.typography.labelSmall,
                color = if (overloaded) scheme.error else scheme.onSurfaceVariant,
            )
        }
    }
}
