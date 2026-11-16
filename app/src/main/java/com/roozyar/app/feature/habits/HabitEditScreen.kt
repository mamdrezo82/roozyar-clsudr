package com.roozyar.app.feature.habits

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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
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
import com.roozyar.app.core.design.ConfirmDialog
import java.time.LocalTime
import java.util.Locale
import com.roozyar.app.core.design.RoozYarTimePickerDialog
import com.roozyar.app.core.design.RoozYarTopBar
import com.roozyar.app.core.notifications.rememberNotificationPermissionRequest

@Composable
fun HabitEditScreen(onDone: () -> Unit, viewModel: HabitEditViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var confirmDelete by remember { mutableStateOf(false) }
    var showTime by remember { mutableStateOf(false) }
    val requestNotifications = rememberNotificationPermissionRequest()
    val scheme = MaterialTheme.colorScheme

    LaunchedEffect(state.done) { if (state.done) onDone() }

    Column(Modifier.fillMaxSize()) {
        RoozYarTopBar(
            title = if (state.isNew) "عادت جدید" else "ویرایش عادت",
            onBack = onDone,
            actions = {
                if (!state.isNew && !state.isLoading) {
                    IconButton(onClick = { confirmDelete = true }) {
                        Icon(Icons.Filled.Delete, contentDescription = "حذف عادت", tint = scheme.error)
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
                    label = { Text("نام عادت") },
                    isError = state.titleError,
                    supportingText = if (state.titleError) ({ Text("نام عادت را وارد کنید.") }) else null,
                    singleLine = true,
                )
                Text("آیکون", style = MaterialTheme.typography.titleSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    habitIconKeys.forEach { key ->
                        FilterChip(
                            selected = state.icon == key,
                            onClick = { viewModel.onIconChange(key) },
                            label = { Icon(habitIcon(key), contentDescription = null) },
                            shape = RoundedCornerShape(50),
                        )
                    }
                }
                Text("روزهای تکرار", style = MaterialTheme.typography.titleSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    persianWeek.forEach { day ->
                        FilterChip(
                            selected = day in state.weekdays,
                            onClick = { viewModel.onWeekdayToggle(day) },
                            label = { Text(weekdayLetter(day)) },
                            shape = RoundedCornerShape(50),
                        )
                    }
                }
                if (state.weekdaysError) {
                    Text("حداقل یک روز را انتخاب کنید.", color = scheme.error, style = MaterialTheme.typography.bodySmall)
                }
                Text("هدف روزانه", style = MaterialTheme.typography.titleSmall)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(onClick = { viewModel.onTargetChange(state.targetCount - 1) }, enabled = state.targetCount > 1) {
                        Icon(Icons.Filled.Remove, contentDescription = "کم کردن")
                    }
                    Text(state.targetCount.toPersianDigits(), style = MaterialTheme.typography.titleMedium)
                    IconButton(onClick = { viewModel.onTargetChange(state.targetCount + 1) }) {
                        Icon(Icons.Filled.Add, contentDescription = "افزودن")
                    }
                    Text(
                        if (state.targetCount == 1) "یک بار در روز" else "بار در روز",
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant,
                    )
                }
                if (state.targetCount > 1) {
                    OutlinedTextField(
                        value = state.unit,
                        onValueChange = viewModel::onUnitChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("واحد (مثلاً لیوان)") },
                        singleLine = true,
                    )
                }
                Text("یادآوری روزانه", style = MaterialTheme.typography.titleSmall)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    AssistChip(
                        onClick = { showTime = true },
                        label = {
                            Text(
                                state.reminderMinute?.let { m -> String.format(Locale.ROOT, "%02d:%02d", m / 60, m % 60).toPersianDigits() }
                                    ?: "افزودن یادآوری",
                            )
                        },
                        leadingIcon = { Icon(Icons.Outlined.Schedule, contentDescription = null) },
                    )
                    if (state.reminderMinute != null) {
                        IconButton(onClick = { viewModel.onReminderChange(null) }) { Icon(Icons.Filled.Close, contentDescription = "حذف یادآوری") }
                    }
                }
                state.error?.let { Text(it, color = scheme.error, style = MaterialTheme.typography.bodyMedium) }
                Button(
                    onClick = viewModel::save,
                    enabled = !state.isSaving,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = scheme.primaryContainer, contentColor = scheme.onPrimaryContainer),
                ) { Text(if (state.isNew) "افزودن عادت" else "ذخیره تغییرات", style = MaterialTheme.typography.labelLarge) }
            }
        }
    }

    if (showTime) {
        RoozYarTimePickerDialog(
            initial = state.reminderMinute?.let { LocalTime.of(it / 60, it % 60) },
            onConfirm = { viewModel.onReminderChange(it.hour * 60 + it.minute); showTime = false; requestNotifications() },
            onDismiss = { showTime = false },
        )
    }
    if (confirmDelete) {
        ConfirmDialog(
            title = "حذف عادت",
            message = "این عادت و تمام سابقه انجام آن برای همیشه حذف می‌شود.",
            confirmLabel = "حذف",
            destructive = true,
            onConfirm = { confirmDelete = false; viewModel.delete() },
            onDismiss = { confirmDelete = false },
        )
    }
}
