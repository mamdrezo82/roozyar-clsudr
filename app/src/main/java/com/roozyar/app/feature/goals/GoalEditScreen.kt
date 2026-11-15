package com.roozyar.app.feature.goals

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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.roozyar.app.core.common.toPersianDigits
import com.roozyar.app.core.date.JalaliFormatter
import com.roozyar.app.core.design.ConfirmDialog
import com.roozyar.app.core.design.JalaliDatePickerDialog
import com.roozyar.app.core.design.RoozYarTopBar
import com.roozyar.app.domain.model.GoalCategory
import com.roozyar.app.domain.model.TaskPriority
import com.roozyar.app.feature.tasks.priorityLabel

@Composable
fun GoalEditScreen(onDone: () -> Unit, viewModel: GoalEditViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showDate by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    val scheme = MaterialTheme.colorScheme

    LaunchedEffect(state.done) { if (state.done) onDone() }

    Column(Modifier.fillMaxSize()) {
        RoozYarTopBar(
            title = if (state.isNew) "هدف جدید" else "ویرایش هدف",
            onBack = onDone,
            actions = {
                if (!state.isNew && !state.isLoading) {
                    IconButton(onClick = { confirmDelete = true }) {
                        Icon(Icons.Filled.Delete, contentDescription = "حذف هدف", tint = scheme.error)
                    }
                }
            },
        )
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                OutlinedTextField(
                    value = state.title,
                    onValueChange = viewModel::onTitleChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("عنوان هدف") },
                    isError = state.titleError,
                    supportingText = if (state.titleError) ({ Text("عنوان را وارد کنید.") }) else null,
                    singleLine = true,
                )
                OutlinedTextField(
                    value = state.description,
                    onValueChange = viewModel::onDescriptionChange,
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    label = { Text("توضیحات (اختیاری)") },
                )
                Text("دسته", style = MaterialTheme.typography.titleSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GoalCategory.entries.forEach { c ->
                        FilterChip(
                            selected = state.category == c,
                            onClick = { viewModel.onCategoryChange(c) },
                            label = { Text(c.label) },
                            shape = RoundedCornerShape(50),
                        )
                    }
                }
                Text("اولویت", style = MaterialTheme.typography.titleSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TaskPriority.entries.forEach { p ->
                        FilterChip(
                            selected = state.priority == p,
                            onClick = { viewModel.onPriorityChange(p) },
                            label = { Text(priorityLabel(p)) },
                            shape = RoundedCornerShape(50),
                        )
                    }
                }
                Text("مهلت نهایی", style = MaterialTheme.typography.titleSmall)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    val d = state.deadline
                    AssistChip(
                        onClick = { showDate = true },
                        label = {
                            Text(d?.let { "${it.day.toPersianDigits()} ${JalaliFormatter.monthName(it.month)} ${it.year.toPersianDigits()}" } ?: "انتخاب تاریخ")
                        },
                        leadingIcon = { Icon(Icons.Outlined.CalendarMonth, contentDescription = null) },
                    )
                    if (d != null) {
                        IconButton(onClick = { viewModel.onDeadlineChange(null) }) {
                            Icon(Icons.Filled.Close, contentDescription = "حذف مهلت")
                        }
                    }
                }
                state.error?.let { Text(it, color = scheme.error, style = MaterialTheme.typography.bodyMedium) }
                Button(
                    onClick = viewModel::save,
                    enabled = !state.isSaving,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = scheme.primaryContainer, contentColor = scheme.onPrimaryContainer),
                ) { Text(if (state.isNew) "افزودن هدف" else "ذخیره تغییرات", style = MaterialTheme.typography.labelLarge) }
            }
        }
    }

    if (showDate) {
        JalaliDatePickerDialog(
            initial = state.deadline,
            today = viewModel.today,
            onConfirm = { viewModel.onDeadlineChange(it); showDate = false },
            onDismiss = { showDate = false },
        )
    }
    if (confirmDelete) {
        ConfirmDialog(
            title = "حذف هدف",
            message = "این هدف و مایلستون‌هایش حذف می‌شوند. وظایف مرتبط حذف نمی‌شوند.",
            confirmLabel = "حذف",
            destructive = true,
            onConfirm = { confirmDelete = false; viewModel.delete() },
            onDismiss = { confirmDelete = false },
        )
    }
}
