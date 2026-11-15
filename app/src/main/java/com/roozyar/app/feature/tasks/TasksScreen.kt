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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.roozyar.app.core.design.ConfirmDialog
import com.roozyar.app.core.design.EmptyState
import com.roozyar.app.core.design.RoozYarTopBar
import java.time.Instant
import java.time.ZoneId

@Composable
fun TasksScreen(
    onBack: () -> Unit,
    onAddTask: () -> Unit,
    onEditTask: (Long) -> Unit,
    viewModel: TasksViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val zone = remember { ZoneId.systemDefault() }
    val now = Instant.now()

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            RoozYarTopBar("وظایف", onBack = onBack)
            OutlinedTextField(
                value = state.query,
                onValueChange = viewModel::onQueryChange,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                placeholder = { Text("جست‌وجوی وظایف") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    if (state.query.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onQueryChange("") }) {
                            Icon(Icons.Filled.Close, contentDescription = "پاک‌کردن جست‌وجو")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(50),
            )
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TaskFilter.entries.forEach { f ->
                    FilterChip(
                        selected = state.filter == f,
                        onClick = { viewModel.onFilterChange(f) },
                        label = { Text(f.label) },
                    )
                }
            }
            when {
                state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                state.error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(state.error!!, color = MaterialTheme.colorScheme.error)
                }
                !state.hasAnyTasks -> EmptyState("هنوز وظیفه‌ای ندارید", "با دکمه «وظیفه جدید» اولین وظیفه را ثبت کنید.")
                state.tasks.isEmpty() -> EmptyState("نتیجه‌ای پیدا نشد", "جست‌وجو یا فیلتر را تغییر دهید.")
                else -> LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(state.tasks, key = { it.id }) { task ->
                        TaskItem(
                            task = task,
                            now = now,
                            zone = zone,
                            onToggle = { viewModel.onToggle(task) },
                            onClick = { onEditTask(task.id) },
                            onDelete = { viewModel.onDeleteRequest(task) },
                        )
                    }
                }
            }
        }
        ExtendedFloatingActionButton(
            onClick = onAddTask,
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            icon = { Icon(Icons.Filled.Add, contentDescription = null) },
            text = { Text("وظیفه جدید") },
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
