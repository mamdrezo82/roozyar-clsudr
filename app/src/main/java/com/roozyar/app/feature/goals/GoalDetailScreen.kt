package com.roozyar.app.feature.goals

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.roozyar.app.core.design.AnimatedLinearProgress
import com.roozyar.app.core.common.toPersianDigits
import com.roozyar.app.core.design.RoozYarCard
import com.roozyar.app.core.design.RoozYarTopBar
import com.roozyar.app.domain.model.TaskPriority
import com.roozyar.app.feature.tasks.TaskItem
import com.roozyar.app.feature.tasks.priorityColor
import com.roozyar.app.feature.tasks.priorityLabel

@Composable
fun GoalDetailScreen(
    onBack: () -> Unit,
    onEdit: (Long) -> Unit,
    onEditTask: (Long) -> Unit,
    viewModel: GoalDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val scheme = MaterialTheme.colorScheme

    Column(Modifier.fillMaxSize()) {
        val success = state as? GoalDetailUiState.Success
        RoozYarTopBar(
            title = "جزئیات هدف",
            onBack = onBack,
            actions = {
                if (success != null) {
                    IconButton(onClick = { onEdit(success.goal.id) }) { Icon(Icons.Filled.Edit, contentDescription = "ویرایش هدف") }
                }
            },
        )
        when (val s = state) {
            GoalDetailUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            GoalDetailUiState.NotFound -> {
                // The goal was deleted (e.g. from the edit screen): leave this screen.
                LaunchedEffect(Unit) { onBack() }
            }
            is GoalDetailUiState.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(s.message, color = scheme.error)
            }
            is GoalDetailUiState.Success -> {
                val goal = s.goal
                var newMilestone by remember { mutableStateOf("") }
                Column(
                    Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Tag(goal.category.label, scheme.tertiary)
                        if (goal.priority != TaskPriority.NORMAL) Tag(priorityLabel(goal.priority), priorityColor(goal.priority))
                        if (goal.completed) Tag("تکمیل‌شده", scheme.primary)
                    }
                    Text(goal.title, style = MaterialTheme.typography.headlineSmall)
                    if (goal.description.isNotBlank()) {
                        Text(goal.description, style = MaterialTheme.typography.bodyMedium, color = scheme.onSurfaceVariant)
                    }
                    goal.deadline?.let {
                        val status = deadlineStatus(goal, s.today)
                        Text(
                            "مهلت: ${formatDeadline(it)}" + (status?.let { t -> " • $t" } ?: ""),
                            style = MaterialTheme.typography.labelLarge,
                            color = if (goal.isOverdue(s.today)) scheme.error else scheme.onSurfaceVariant,
                        )
                    }
                    RoozYarCard(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row {
                                Text("پیشرفت", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                                Text("٪" + (goal.progress * 100).toInt().toPersianDigits(), style = MaterialTheme.typography.titleMedium, color = scheme.primary)
                            }
                            AnimatedLinearProgress(
                                progress = goal.progress,
                                modifier = Modifier.fillMaxWidth().height(8.dp),
                                color = scheme.primary,
                                trackColor = scheme.surfaceContainerHighest,
                            )
                        }
                    }

                    Text("مایلستون‌ها", style = MaterialTheme.typography.titleMedium)
                    if (goal.milestones.isEmpty()) {
                        Text("هنوز مرحله‌ای تعریف نشده. هدف بزرگ را به مرحله‌های کوچک تقسیم کنید.", style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant)
                    }
                    goal.milestones.forEach { m ->
                        RoozYarCard(Modifier.fillMaxWidth(), onClick = { viewModel.toggleMilestone(m) }) {
                            Row(Modifier.padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { viewModel.toggleMilestone(m) }) {
                                    Icon(
                                        if (m.done) Icons.Filled.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                                        contentDescription = if (m.done) "لغو انجام" else "انجام شد",
                                        tint = if (m.done) scheme.primary else scheme.onSurfaceVariant,
                                    )
                                }
                                Text(
                                    m.title,
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = if (m.done) scheme.onSurfaceVariant else scheme.onSurface,
                                    textDecoration = if (m.done) TextDecoration.LineThrough else null,
                                )
                                IconButton(onClick = { viewModel.deleteMilestone(m) }) {
                                    Icon(Icons.Filled.Close, contentDescription = "حذف مرحله", tint = scheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = newMilestone,
                            onValueChange = { newMilestone = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("مرحله جدید") },
                            singleLine = true,
                            shape = RoundedCornerShape(50),
                        )
                        IconButton(
                            onClick = { viewModel.addMilestone(newMilestone); newMilestone = "" },
                            enabled = newMilestone.isNotBlank(),
                        ) { Icon(Icons.Filled.Add, contentDescription = "افزودن مرحله") }
                    }

                    if (s.tasks.isNotEmpty()) {
                        Text("وظایف مرتبط", style = MaterialTheme.typography.titleMedium)
                        s.tasks.forEach { task ->
                            TaskItem(
                                task = task,
                                now = s.now,
                                zone = s.zone,
                                onToggle = { viewModel.toggleTask(task) },
                                onClick = { onEditTask(task.id) },
                            )
                        }
                    }

                    if (goal.completed) {
                        OutlinedButton(
                            onClick = { viewModel.toggleCompleted(goal) },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(50),
                        ) { Text("بازگشایی هدف", style = MaterialTheme.typography.labelLarge) }
                    } else {
                        Button(
                            onClick = { viewModel.toggleCompleted(goal) },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(containerColor = scheme.primaryContainer, contentColor = scheme.onPrimaryContainer),
                        ) { Text("تکمیل هدف", style = MaterialTheme.typography.labelLarge) }
                    }
                }
            }
        }
    }
}
