package com.roozyar.app.feature.tasks

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
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Schedule
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
import com.roozyar.app.core.design.JalaliDatePickerDialog
import com.roozyar.app.core.design.RoozYarTimePickerDialog
import com.roozyar.app.core.design.RoozYarTopBar
import com.roozyar.app.domain.model.TaskPriority
import java.util.Locale

@Composable
fun TaskEditScreen(
    onDone: () -> Unit,
    viewModel: TaskEditViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    LaunchedEffect(state.saved) {
        if (state.saved) onDone()
    }

    Column(Modifier.fillMaxSize()) {
        RoozYarTopBar(if (state.isNew) "وظیفه جدید" else "ویرایش وظیفه", onBack = onDone)
        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            state.loadError != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(state.loadError!!, color = MaterialTheme.colorScheme.error)
            }
            else -> Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                OutlinedTextField(
                    value = state.title,
                    onValueChange = viewModel::onTitleChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("عنوان") },
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
                Text("اولویت", style = MaterialTheme.typography.titleSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TaskPriority.entries.forEach { p ->
                        FilterChip(
                            selected = state.priority == p,
                            onClick = { viewModel.onPriorityChange(p) },
                            label = { Text(priorityLabel(p)) },
                        )
                    }
                }
                Text("مهلت", style = MaterialTheme.typography.titleSmall)
                val date = state.dueDate
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    AssistChip(
                        onClick = { showDatePicker = true },
                        label = {
                            Text(
                                date?.let { "${it.day.toPersianDigits()} ${JalaliFormatter.monthName(it.month)} ${it.year.toPersianDigits()}" }
                                    ?: "انتخاب تاریخ",
                            )
                        },
                        leadingIcon = { Icon(Icons.Outlined.CalendarMonth, contentDescription = null) },
                    )
                    if (date != null) {
                        IconButton(onClick = { viewModel.onDueDateChange(null) }) {
                            Icon(Icons.Filled.Close, contentDescription = "حذف مهلت")
                        }
                    }
                }
                if (date != null) {
                    val time = state.dueTime
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                        AssistChip(
                            onClick = { showTimePicker = true },
                            label = {
                                Text(
                                    time?.let { String.format(Locale.ROOT, "%02d:%02d", it.hour, it.minute).toPersianDigits() }
                                        ?: "افزودن ساعت",
                                )
                            },
                            leadingIcon = { Icon(Icons.Outlined.Schedule, contentDescription = null) },
                        )
                        if (time != null) {
                            IconButton(onClick = { viewModel.onDueTimeChange(null) }) {
                                Icon(Icons.Filled.Close, contentDescription = "حذف ساعت")
                            }
                        }
                    }
                }
                state.saveError?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium) }
                Button(
                    onClick = viewModel::save,
                    enabled = !state.isSaving,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    ),
                ) { Text(if (state.isNew) "افزودن وظیفه" else "ذخیره تغییرات", style = MaterialTheme.typography.labelLarge) }
            }
        }
    }

    if (showDatePicker) {
        JalaliDatePickerDialog(
            initial = state.dueDate,
            today = viewModel.today,
            onConfirm = { viewModel.onDueDateChange(it); showDatePicker = false },
            onDismiss = { showDatePicker = false },
        )
    }
    if (showTimePicker) {
        RoozYarTimePickerDialog(
            initial = state.dueTime,
            onConfirm = { viewModel.onDueTimeChange(it); showTimePicker = false },
            onDismiss = { showTimePicker = false },
        )
    }
}
