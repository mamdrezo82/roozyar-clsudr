package com.roozyar.app.feature.goals

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.roozyar.app.core.common.toPersianDigits
import com.roozyar.app.core.design.EmptyState
import com.roozyar.app.core.design.RoozYarCard
import com.roozyar.app.core.design.RoozYarTopBar
import com.roozyar.app.domain.model.Goal
import com.roozyar.app.domain.model.GoalCategory
import com.roozyar.app.domain.model.TaskPriority
import com.roozyar.app.feature.tasks.priorityColor
import com.roozyar.app.feature.tasks.priorityLabel
import java.time.LocalDate

@Composable
fun GoalsScreen(
    onAddGoal: () -> Unit,
    onOpenGoal: (Long) -> Unit,
    viewModel: GoalsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val scheme = MaterialTheme.colorScheme

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            RoozYarTopBar("اهداف و مراحل")
            when {
                state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                state.error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(state.error!!, color = scheme.error)
                }
                state.activeCount + state.completedCount == 0 ->
                    EmptyState("هنوز هدفی ندارید", "با دکمه «هدف جدید» اولین هدف را تعریف و آن را به مرحله‌های کوچک تقسیم کنید.")
                else -> LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item(key = "summary") { SummaryCard(state) }
                    item(key = "filters") {
                        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = state.filter == null,
                                onClick = { viewModel.onFilterChange(null) },
                                label = { Text("همه") },
                                shape = RoundedCornerShape(50),
                            )
                            GoalCategory.entries.forEach { c ->
                                FilterChip(
                                    selected = state.filter == c,
                                    onClick = { viewModel.onFilterChange(c) },
                                    label = { Text(c.label) },
                                    shape = RoundedCornerShape(50),
                                )
                            }
                        }
                    }
                    if (state.active.isEmpty() && state.completed.isEmpty()) {
                        item(key = "none") {
                            Text(
                                "در این دسته هدفی نیست.",
                                modifier = Modifier.padding(vertical = 24.dp),
                                color = scheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                    items(state.active, key = { "a${it.id}" }) { goal -> GoalCard(goal, state.today) { onOpenGoal(goal.id) } }
                    if (state.completed.isNotEmpty()) {
                        item(key = "doneHeader") {
                            Text(
                                "تکمیل‌شده (${state.completed.size.toPersianDigits()})",
                                style = MaterialTheme.typography.titleSmall,
                                color = scheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 8.dp),
                            )
                        }
                        items(state.completed, key = { "c${it.id}" }) { goal -> GoalCard(goal, state.today) { onOpenGoal(goal.id) } }
                    }
                }
            }
        }
        ExtendedFloatingActionButton(
            onClick = onAddGoal,
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
            containerColor = scheme.primaryContainer,
            contentColor = scheme.onPrimaryContainer,
            shape = RoundedCornerShape(50),
            icon = { Icon(Icons.Filled.Add, contentDescription = null) },
            text = { Text("هدف جدید", style = MaterialTheme.typography.labelLarge) },
        )
    }
}

@Composable
private fun SummaryCard(state: GoalsUiState) {
    val scheme = MaterialTheme.colorScheme
    RoozYarCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("پیشرفت کلی اهداف فعال", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                Text(
                    "٪" + (state.overallProgress * 100).toInt().toPersianDigits(),
                    style = MaterialTheme.typography.titleMedium,
                    color = scheme.primary,
                )
            }
            LinearProgressIndicator(
                progress = { state.overallProgress },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = scheme.primary,
                trackColor = scheme.surfaceContainerHighest,
            )
            Text(
                "${state.activeCount.toPersianDigits()} هدف فعال • ${state.completedCount.toPersianDigits()} تکمیل‌شده",
                style = MaterialTheme.typography.labelMedium,
                color = scheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
internal fun GoalCard(goal: Goal, today: LocalDate, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    RoozYarCard(Modifier.fillMaxWidth(), onClick = onClick) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Tag(goal.category.label, scheme.tertiary)
                if (goal.priority != TaskPriority.NORMAL && !goal.completed) {
                    Tag(priorityLabel(goal.priority), priorityColor(goal.priority))
                }
            }
            Text(
                goal.title,
                style = MaterialTheme.typography.titleMedium,
                color = if (goal.completed) scheme.onSurfaceVariant else scheme.onSurface,
                textDecoration = if (goal.completed) TextDecoration.LineThrough else null,
            )
            goal.deadline?.let {
                val status = deadlineStatus(goal, today)
                Text(
                    "مهلت: ${formatDeadline(it)}" + (status?.let { s -> " • $s" } ?: ""),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (goal.isOverdue(today)) scheme.error else scheme.onSurfaceVariant,
                )
            }
            LinearProgressIndicator(
                progress = { goal.progress },
                modifier = Modifier.fillMaxWidth().height(6.dp),
                color = if (goal.completed) scheme.primary else scheme.secondary,
                trackColor = scheme.surfaceContainerHighest,
            )
            Text(
                if (goal.milestones.isEmpty()) "بدون مایلستون"
                else "${goal.doneMilestones.toPersianDigits()} از ${goal.milestones.size.toPersianDigits()} مایلستون",
                style = MaterialTheme.typography.labelSmall,
                color = scheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
internal fun Tag(text: String, color: androidx.compose.ui.graphics.Color) {
    Surface(shape = MaterialTheme.shapes.small, color = color.copy(alpha = 0.15f)) {
        Text(text, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = color)
    }
}
