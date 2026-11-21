package com.roozyar.app.feature.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.roozyar.app.core.common.toPersianDigits
import com.roozyar.app.core.date.JalaliFormatter
import com.roozyar.app.core.design.RoozYarCard
import com.roozyar.app.domain.model.DashboardCard
import com.roozyar.app.feature.achievements.LevelCard
import com.roozyar.app.feature.calendar.formatEventTime
import com.roozyar.app.feature.goals.GoalCard
import com.roozyar.app.feature.tasks.TaskItem

@Composable
fun DashboardScreen(
    onAddTask: () -> Unit,
    onAddEvent: () -> Unit,
    onAddJournal: () -> Unit,
    onOpenTasks: () -> Unit,
    onEditTask: (Long) -> Unit,
    onOpenHabits: () -> Unit,
    onOpenGoals: () -> Unit,
    onOpenGoal: (Long) -> Unit,
    onOpenCalendar: () -> Unit,
    onEditEvent: (Long) -> Unit,
    onOpenFocus: () -> Unit,
    onOpenAchievements: () -> Unit,
    onCustomize: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val scheme = MaterialTheme.colorScheme
    when (val s = state) {
        DashboardUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        is DashboardUiState.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(s.message, color = scheme.error, style = MaterialTheme.typography.bodyLarge)
        }
        is DashboardUiState.Success -> Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(s.greeting, style = MaterialTheme.typography.headlineSmall)
                    Text(JalaliFormatter.full(s.today), style = MaterialTheme.typography.bodyMedium, color = scheme.onSurfaceVariant)
                }
                IconButton(onClick = onCustomize) { Icon(Icons.Outlined.Tune, contentDescription = "سفارشی‌سازی صفحه خانه") }
            }
            if (s.cards.isEmpty()) {
                Text(
                    "همه کارت‌ها پنهان هستند. با دکمه سفارشی‌سازی بالا آن‌ها را برگردانید.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant,
                )
            }
            s.cards.forEach { card ->
                when (card) {
                    DashboardCard.PROGRESS -> ProgressCard(s.summary)
                    DashboardCard.HABITS -> HabitsCard(s.habits, onOpenHabits)
                    DashboardCard.TODAY_TASKS -> TodayTasks(s, viewModel, onOpenTasks, onEditTask)
                    DashboardCard.EVENTS -> TodayEvents(s, onOpenCalendar, onEditEvent)
                    DashboardCard.FOCUS -> FocusCard(s.focusTodayMinutes, onOpenFocus)
                    DashboardCard.GOALS -> GoalsSection(s, onOpenGoals, onOpenGoal)
                    DashboardCard.LEVEL -> LevelCard(s.level, onOpenAchievements)
                    DashboardCard.QUICK_ACTIONS -> QuickActions(onAddTask, onAddEvent, onAddJournal)
                }
            }
        }
    }
}

@Composable
private fun TodayTasks(s: DashboardUiState.Success, viewModel: DashboardViewModel, onOpenTasks: () -> Unit, onEditTask: (Long) -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("وظایف امروز", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            TextButton(onClick = onOpenTasks) { Text("همه وظایف") }
        }
        if (s.summary.todayOpen.isEmpty()) {
            Text(
                if (s.summary.totalToday == 0) "برای امروز وظیفه‌ای ثبت نشده است." else "همه وظایف امروز انجام شده‌اند.",
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onSurfaceVariant,
            )
        } else {
            s.summary.todayOpen.forEach { task ->
                TaskItem(
                    task = task,
                    now = s.now,
                    zone = viewModel.zone,
                    onToggle = { viewModel.onToggle(task) },
                    onClick = { onEditTask(task.id) },
                )
            }
        }
    }
}

@Composable
private fun TodayEvents(s: DashboardUiState.Success, onOpenCalendar: () -> Unit, onEditEvent: (Long) -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("رویدادهای امروز", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            TextButton(onClick = onOpenCalendar) { Text("تقویم") }
        }
        if (s.todayEvents.isEmpty()) {
            Text("برای امروز رویدادی ثبت نشده است.", style = MaterialTheme.typography.bodyMedium, color = scheme.onSurfaceVariant)
        }
        s.todayEvents.forEach { e ->
            RoozYarCard(Modifier.fillMaxWidth(), onClick = { onEditEvent(e.id) }) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(e.title, style = MaterialTheme.typography.titleSmall)
                    Text(
                        formatEventTime(e, s.zone) + if (e.location.isNotBlank()) " • ${e.location}" else "",
                        style = MaterialTheme.typography.labelMedium,
                        color = scheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun FocusCard(minutes: Int, onOpenFocus: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    RoozYarCard(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("تمرکز امروز", style = MaterialTheme.typography.titleSmall)
                Text("${minutes.toPersianDigits()} دقیقه", style = MaterialTheme.typography.headlineSmall, color = scheme.primary)
            }
            OutlinedButton(onClick = onOpenFocus, shape = RoundedCornerShape(50)) { Text("شروع تمرکز") }
        }
    }
}

@Composable
private fun GoalsSection(s: DashboardUiState.Success, onOpenGoals: () -> Unit, onOpenGoal: (Long) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("اهداف فعال", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            TextButton(onClick = onOpenGoals) { Text("همه اهداف") }
        }
        if (s.goals.isEmpty()) {
            Text("هدف فعالی ندارید.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        s.goals.forEach { goal -> GoalCard(goal, s.localToday) { onOpenGoal(goal.id) } }
    }
}

@Composable
private fun QuickActions(onAddTask: () -> Unit, onAddEvent: () -> Unit, onAddJournal: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("دسترسی سریع", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AssistChip(onClick = onAddEvent, label = { Text("رویداد") }, leadingIcon = { Icon(Icons.Filled.Add, contentDescription = null) })
            AssistChip(onClick = onAddJournal, label = { Text("یادداشت") }, leadingIcon = { Icon(Icons.Filled.Add, contentDescription = null) })
        }
        Button(
            onClick = onAddTask,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(containerColor = scheme.primaryContainer, contentColor = scheme.onPrimaryContainer),
        ) { Text("وظیفه جدید", style = MaterialTheme.typography.labelLarge) }
    }
}

@Composable
private fun ProgressCard(summary: TodaySummary) {
    RoozYarCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("پیشرفت امروز", style = MaterialTheme.typography.titleSmall)
            if (summary.totalToday == 0) {
                Text(
                    "وظیفه‌ای با مهلت امروز ندارید.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Text(
                    "${summary.doneToday.toPersianDigits()} از ${summary.totalToday.toPersianDigits()} وظیفه انجام شده است",
                    style = MaterialTheme.typography.bodyMedium,
                )
                LinearProgressIndicator(
                    progress = { summary.progress },
                    modifier = Modifier.fillMaxWidth().height(8.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                )
            }
            if (summary.overdueCount > 0) {
                Text(
                    "${summary.overdueCount.toPersianDigits()} وظیفه عقب‌افتاده",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
private fun HabitsCard(habits: HabitsToday, onClick: () -> Unit) {
    RoozYarCard(Modifier.fillMaxWidth(), onClick = onClick) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("عادت‌های امروز", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                if (habits.total > 0) {
                    Text(
                        "${habits.done.toPersianDigits()} از ${habits.total.toPersianDigits()}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            if (habits.total == 0) {
                Text("امروز عادتی برنامه‌ریزی نشده است.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                LinearProgressIndicator(
                    progress = { habits.progress },
                    modifier = Modifier.fillMaxWidth().height(8.dp),
                    color = MaterialTheme.colorScheme.secondary,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                )
            }
        }
    }
}
