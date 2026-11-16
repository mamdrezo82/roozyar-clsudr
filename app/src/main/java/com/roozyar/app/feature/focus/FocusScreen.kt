package com.roozyar.app.feature.focus

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.roozyar.app.core.common.toPersianDigits
import com.roozyar.app.core.date.JalaliFormatter
import com.roozyar.app.core.date.toJalali
import com.roozyar.app.core.design.ConfirmDialog
import com.roozyar.app.core.design.RoozYarCard
import com.roozyar.app.core.design.RoozYarTopBar
import com.roozyar.app.domain.model.FocusKind
import com.roozyar.app.domain.model.FocusSession
import java.time.ZoneId
import java.util.Locale

fun formatClock(totalSeconds: Long): String =
    String.format(Locale.ROOT, "%02d:%02d", totalSeconds / 60, totalSeconds % 60).toPersianDigits()

@Composable
fun FocusScreen(onBack: () -> Unit, viewModel: FocusViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val scheme = MaterialTheme.colorScheme
    var confirmStop by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf<FocusSession?>(null) }

    // Keep the screen awake while a timer is running.
    val view = LocalView.current
    val running = state.active != null
    SideEffect { view.keepScreenOn = running }

    Column(Modifier.fillMaxSize()) {
        RoozYarTopBar("تمرکز", onBack = onBack)
        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            state.error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(state.error!!, color = scheme.error) }
            else -> Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                val active = state.active
                if (active != null) {
                    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                progress = { state.progress },
                                modifier = Modifier.size(220.dp),
                                color = if (active.kind == FocusKind.WORK) scheme.primary else scheme.secondary,
                                trackColor = scheme.surfaceContainerHighest,
                                strokeWidth = 10.dp,
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(formatClock(state.remainingSeconds), style = MaterialTheme.typography.displayMedium)
                                Text(
                                    if (active.kind == FocusKind.WORK) "در حال تمرکز" else "استراحت",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = scheme.onSurfaceVariant,
                                )
                            }
                        }
                        val link = active.taskId?.let { state.taskTitles[it] } ?: active.goalId?.let { state.goalTitles[it] }
                        if (link != null) Text(link, style = MaterialTheme.typography.bodyMedium, color = scheme.onSurfaceVariant)
                        OutlinedButton(onClick = { confirmStop = true }, shape = RoundedCornerShape(50), modifier = Modifier.fillMaxWidth().height(52.dp)) {
                            Text(if (active.kind == FocusKind.WORK) "توقف جلسه" else "پایان استراحت", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                } else {
                    Text("مدت جلسه", style = MaterialTheme.typography.titleSmall)
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        focusPresetMinutes.forEach { m ->
                            FilterChip(
                                selected = state.selection.minutes == m,
                                onClick = { viewModel.onMinutes(m) },
                                label = { Text("${m.toPersianDigits()} دقیقه") },
                                shape = RoundedCornerShape(50),
                            )
                        }
                    }
                    if (state.openTasks.isNotEmpty()) {
                        Text("وظیفه مرتبط (اختیاری)", style = MaterialTheme.typography.titleSmall)
                        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(selected = state.selection.taskId == null, onClick = { viewModel.onTask(null) }, label = { Text("بدون وظیفه") }, shape = RoundedCornerShape(50))
                            state.openTasks.take(12).forEach { t ->
                                FilterChip(selected = state.selection.taskId == t.id, onClick = { viewModel.onTask(t.id) }, label = { Text(t.title) }, shape = RoundedCornerShape(50))
                            }
                        }
                    }
                    if (state.goals.isNotEmpty()) {
                        Text("هدف مرتبط (اختیاری)", style = MaterialTheme.typography.titleSmall)
                        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(selected = state.selection.goalId == null, onClick = { viewModel.onGoal(null) }, label = { Text("بدون هدف") }, shape = RoundedCornerShape(50))
                            state.goals.forEach { g ->
                                FilterChip(selected = state.selection.goalId == g.id, onClick = { viewModel.onGoal(g.id) }, label = { Text(g.title) }, shape = RoundedCornerShape(50))
                            }
                        }
                    }
                    Button(
                        onClick = viewModel::startFocus,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = scheme.primaryContainer, contentColor = scheme.onPrimaryContainer),
                    ) {
                        Icon(Icons.Outlined.Timer, contentDescription = null)
                        Text("  شروع تمرکز", style = MaterialTheme.typography.labelLarge)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        breakPresetMinutes.forEach { m ->
                            OutlinedButton(onClick = { viewModel.startBreak(m) }, shape = RoundedCornerShape(50), modifier = Modifier.weight(1f)) {
                                Text("استراحت ${m.toPersianDigits()} دقیقه")
                            }
                        }
                    }
                }

                StatsCard(state)

                if (state.recent.isNotEmpty()) {
                    Text("جلسه‌های اخیر", style = MaterialTheme.typography.titleMedium)
                    state.recent.forEach { s ->
                        SessionRow(s, state, onDelete = { deleting = s })
                    }
                }
            }
        }
    }

    if (confirmStop) {
        val kind = state.active?.kind
        ConfirmDialog(
            title = if (kind == FocusKind.BREAK) "پایان استراحت" else "توقف جلسه",
            message = if (kind == FocusKind.BREAK) "استراحت پایان یابد؟" else "جلسه متوقف می‌شود. اگر دست‌کم یک دقیقه گذشته باشد، همان مقدار ثبت می‌شود.",
            confirmLabel = if (kind == FocusKind.BREAK) "پایان" else "توقف",
            onConfirm = { confirmStop = false; viewModel.stop() },
            onDismiss = { confirmStop = false },
        )
    }
    deleting?.let { s ->
        ConfirmDialog(
            title = "حذف جلسه",
            message = "این جلسه از گزارش حذف می‌شود.",
            confirmLabel = "حذف",
            destructive = true,
            onConfirm = { deleting = null; viewModel.deleteSession(s.id) },
            onDismiss = { deleting = null },
        )
    }
}

@Composable
private fun StatsCard(state: FocusUiState) {
    val scheme = MaterialTheme.colorScheme
    val stats = state.stats
    RoozYarCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("گزارش تمرکز", style = MaterialTheme.typography.titleSmall)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Stat("دقیقه امروز", stats.todayMinutes)
                Stat("جلسه امروز", stats.todaySessions)
                Stat("دقیقه این هفته", stats.weekMinutes)
            }
            val max = (stats.last7Days.maxOrNull() ?: 0).coerceAtLeast(1)
            Row(Modifier.fillMaxWidth().height(64.dp), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.Bottom) {
                stats.last7Days.forEach { m ->
                    Box(
                        Modifier.weight(1f).height((8 + 56 * m / max).dp).background(
                            if (m > 0) scheme.primary else scheme.surfaceContainerHighest,
                            RoundedCornerShape(4.dp),
                        ),
                    )
                }
            }
            Text("۷ روز اخیر", style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun Stat(label: String, value: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value.toPersianDigits(), style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SessionRow(s: FocusSession, state: FocusUiState, onDelete: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val zone = remember { ZoneId.systemDefault() }
    val j = s.date(zone).toJalali()
    val link = s.taskId?.let { state.taskTitles[it] } ?: s.goalId?.let { state.goalTitles[it] }
    RoozYarCard(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(
                Icons.Outlined.CheckCircle,
                contentDescription = if (s.completed) "کامل" else "نیمه‌کاره",
                tint = if (s.completed) scheme.primary else scheme.onSurfaceVariant,
            )
            Column(Modifier.weight(1f)) {
                Text("${s.actualMinutes.toPersianDigits()} دقیقه" + (link?.let { " • $it" } ?: ""), style = MaterialTheme.typography.titleSmall)
                Text(
                    "${j.day.toPersianDigits()} ${JalaliFormatter.monthName(j.month)}" + if (!s.completed) " • زودتر متوقف شد" else "",
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onDelete) { Icon(Icons.Filled.Close, contentDescription = "حذف جلسه", tint = scheme.onSurfaceVariant) }
        }
    }
}
