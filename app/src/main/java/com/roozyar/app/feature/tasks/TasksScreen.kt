package com.roozyar.app.feature.tasks

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.roozyar.app.core.common.toPersianDigits
import com.roozyar.app.core.design.ConfirmDialog
import com.roozyar.app.core.design.EmptyState
import com.roozyar.app.core.design.RoozYarCard
import com.roozyar.app.core.design.RoozYarTopBar
import com.roozyar.app.core.design.SearchField
import com.roozyar.app.domain.model.TaskCategory

@Composable
fun TasksScreen(
    onBack: () -> Unit,
    onAddTask: () -> Unit,
    onEditTask: (Long) -> Unit,
    viewModel: TasksViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val scheme = MaterialTheme.colorScheme

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            RoozYarTopBar("وظایف", onBack = onBack)
            when {
                state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                state.error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(state.error!!, color = scheme.error)
                }
                !state.hasAnyTasks -> EmptyState("هنوز وظیفه‌ای ندارید", "با دکمه «وظیفه جدید» اولین وظیفه را ثبت کنید.")
                else -> LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    item(key = "summary") { SummaryCard(state.summary) }
                    item(key = "search") {
                        SearchField(
                            value = state.query,
                            onValueChange = viewModel::onQueryChange,
                            placeholder = "جست‌وجو در عنوان و توضیحات",
                        )
                    }
                    item(key = "filters") {
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            val s = state.summary
                            TaskFilter.entries.forEach { f ->
                                val count = when (f) {
                                    TaskFilter.ALL -> s.open + s.done
                                    TaskFilter.OPEN -> s.open
                                    TaskFilter.DONE -> s.done
                                }
                                FilterChip(
                                    selected = state.filter == f,
                                    onClick = { viewModel.onFilterChange(f) },
                                    label = { Text("${f.label} (${count.toPersianDigits()})") },
                                    shape = RoundedCornerShape(50),
                                )
                            }
                        }
                    }
                    item(key = "categories") {
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            FilterChip(
                                selected = state.category == null,
                                onClick = { viewModel.onCategoryChange(null) },
                                label = { Text("همه دسته‌ها") },
                                shape = RoundedCornerShape(50),
                            )
                            TaskCategory.entries.filter { it != TaskCategory.NONE }.forEach { c ->
                                FilterChip(
                                    selected = state.category == c,
                                    onClick = { viewModel.onCategoryChange(c) },
                                    label = { Text(c.label) },
                                    shape = RoundedCornerShape(50),
                                )
                            }
                        }
                    }
                    if (state.groups.isEmpty()) {
                        item(key = "empty") {
                            Text(
                                "نتیجه‌ای پیدا نشد. جست‌وجو یا فیلتر را تغییر دهید.",
                                modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyMedium,
                                color = scheme.onSurfaceVariant,
                            )
                        }
                    }
                    state.groups.forEach { group ->
                        item(key = "h_${group.kind.name}") { GroupHeader(group) }
                        items(group.tasks.size, key = { i -> group.tasks[i].id }) { i ->
                            val task = group.tasks[i]
                            TaskItem(
                                task = task,
                                now = state.now,
                                zone = state.zone,
                                onToggle = { viewModel.onToggle(task) },
                                onClick = { onEditTask(task.id) },
                                onDelete = { viewModel.onDeleteRequest(task) },
                            )
                        }
                    }
                }
            }
        }
        ExtendedFloatingActionButton(
            onClick = onAddTask,
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
            containerColor = scheme.primaryContainer,
            contentColor = scheme.onPrimaryContainer,
            shape = RoundedCornerShape(50),
            icon = { Icon(Icons.Filled.Add, contentDescription = null) },
            text = { Text("وظیفه جدید", style = MaterialTheme.typography.labelLarge) },
        )
    }

    state.pendingDelete?.let { task ->
        ConfirmDialog(
            title = "حذف وظیفه",
            message = "«${task.title}» برای همیشه حذف می‌شود.",
            confirmLabel = "حذف",
            destructive = true,
            onConfirm = viewModel::onDeleteConfirm,
            onDismiss = viewModel::onDeleteDismiss,
        )
    }
}

@Composable
private fun GroupHeader(group: TaskGroup) {
    val scheme = MaterialTheme.colorScheme
    val color = when (group.kind) {
        TaskGroupKind.OVERDUE -> scheme.error
        TaskGroupKind.TODAY -> scheme.primary
        else -> scheme.onSurfaceVariant
    }
    Text(
        "${group.kind.title} (${group.tasks.size.toPersianDigits()})",
        modifier = Modifier.padding(top = 12.dp, bottom = 2.dp),
        style = MaterialTheme.typography.titleSmall,
        color = color,
    )
}

@Composable
private fun SummaryCard(summary: TasksSummary) {
    val scheme = MaterialTheme.colorScheme
    RoozYarCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { summary.completion },
                        modifier = Modifier.size(64.dp),
                        color = scheme.primary,
                        trackColor = scheme.surfaceContainerHighest,
                        strokeWidth = 6.dp,
                    )
                    Text(
                        "٪" + (summary.completion * 100).toInt().toPersianDigits(),
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("پیشرفت وظایف", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "${summary.done.toPersianDigits()} از ${(summary.open + summary.done).toPersianDigits()} وظیفه انجام شده",
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant,
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatBox("باز", summary.open, scheme.primary, Modifier.weight(1f))
                StatBox("امروز", summary.dueToday, scheme.secondary, Modifier.weight(1f))
                StatBox("عقب‌افتاده", summary.overdue, if (summary.overdue > 0) scheme.error else scheme.onSurfaceVariant, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun StatBox(label: String, value: Int, color: Color, modifier: Modifier) {
    Surface(modifier = modifier, shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surfaceContainerHigh) {
        Column(Modifier.padding(vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value.toPersianDigits(), style = MaterialTheme.typography.titleMedium, color = color)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
