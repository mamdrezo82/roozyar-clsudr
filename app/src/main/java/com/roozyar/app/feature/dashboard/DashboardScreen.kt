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
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import com.roozyar.app.feature.goals.GoalCard
import com.roozyar.app.feature.tasks.TaskItem
import java.time.Instant

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
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(s.greeting, style = MaterialTheme.typography.headlineSmall)
                Text(JalaliFormatter.full(s.today), style = MaterialTheme.typography.bodyMedium, color = scheme.onSurfaceVariant)
            }
            ProgressCard(s.summary)
            if (s.habits.total > 0) HabitsCard(s.habits, onOpenHabits)

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
                val now = Instant.now()
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    s.summary.todayOpen.forEach { task ->
                        TaskItem(
                            task = task,
                            now = now,
                            zone = viewModel.zone,
                            onToggle = { viewModel.onToggle(task) },
                            onClick = { onEditTask(task.id) },
                        )
                    }
                }
            }

            if (s.goals.isNotEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("اهداف فعال", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                    TextButton(onClick = onOpenGoals) { Text("همه اهداف") }
                }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    s.goals.forEach { goal -> GoalCard(goal, s.localToday) { onOpenGoal(goal.id) } }
                }
            }

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
                Text(
                    "${habits.done.toPersianDigits()} از ${habits.total.toPersianDigits()}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            LinearProgressIndicator(
                progress = { habits.progress },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = MaterialTheme.colorScheme.secondary,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            )
        }
    }
}
