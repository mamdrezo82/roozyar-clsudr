package com.roozyar.app.feature.calendar

import androidx.compose.foundation.horizontalScroll
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
import com.roozyar.app.core.design.ConfirmDialog
import com.roozyar.app.core.design.JalaliDatePickerDialog
import com.roozyar.app.core.design.RoozYarTimePickerDialog
import com.roozyar.app.core.design.RoozYarTopBar
import com.roozyar.app.domain.model.EventCategory
import java.time.LocalTime
import java.util.Locale

private fun LocalTime.label() = String.format(Locale.ROOT, "%02d:%02d", hour, minute).toPersianDigits()

@Composable
fun EventEditScreen(onDone: () -> Unit, viewModel: EventEditViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showDate by remember { mutableStateOf(false) }
    var showStart by remember { mutableStateOf(false) }
    var showEnd by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    val scheme = MaterialTheme.colorScheme

    LaunchedEffect(state.done) { if (state.done) onDone() }

    Column(Modifier.fillMaxSize()) {
        RoozYarTopBar(
            title = if (state.isNew) "رویداد جدید" else "ویرایش رویداد",
            onBack = onDone,
            actions = {
                if (!state.isNew && !state.isLoading) {
                    IconButton(onClick = { confirmDelete = true }) {
                        Icon(Icons.Filled.Delete, contentDescription = "حذف رویداد", tint = scheme.error)
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
                    label = { Text("عنوان رویداد") },
                    isError = state.titleError,
                    supportingText = if (state.titleError) ({ Text("عنوان را وارد کنید.") }) else null,
                    singleLine = true,
                )
                Text("تاریخ", style = MaterialTheme.typography.titleSmall)
                AssistChip(
                    onClick = { showDate = true },
                    label = { Text("${state.date.day.toPersianDigits()} ${JalaliFormatter.monthName(state.date.month)} ${state.date.year.toPersianDigits()}") },
                    leadingIcon = { Icon(Icons.Outlined.CalendarMonth, contentDescription = null) },
                )
                Text("ساعت (اختیاری؛ بدون ساعت یعنی تمام روز)", style = MaterialTheme.typography.titleSmall)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    AssistChip(
                        onClick = { showStart = true },
                        label = { Text(state.startTime?.let { "شروع ${it.label()}" } ?: "افزودن ساعت شروع") },
                        leadingIcon = { Icon(Icons.Outlined.Schedule, contentDescription = null) },
                    )
                    if (state.startTime != null) {
                        IconButton(onClick = { viewModel.onStartTimeChange(null) }) { Icon(Icons.Filled.Close, contentDescription = "حذف ساعت شروع") }
                    }
                }
                if (state.startTime != null) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        AssistChip(
                            onClick = { showEnd = true },
                            label = { Text(state.endTime?.let { "پایان ${it.label()}" } ?: "افزودن ساعت پایان") },
                            leadingIcon = { Icon(Icons.Outlined.Schedule, contentDescription = null) },
                        )
                        if (state.endTime != null) {
                            IconButton(onClick = { viewModel.onEndTimeChange(null) }) { Icon(Icons.Filled.Close, contentDescription = "حذف ساعت پایان") }
                        }
                    }
                }
                if (state.timeError) Text("ساعت پایان باید بعد از شروع باشد.", color = scheme.error, style = MaterialTheme.typography.bodySmall)
                Text("دسته", style = MaterialTheme.typography.titleSmall)
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    EventCategory.entries.forEach { c ->
                        FilterChip(
                            selected = state.category == c,
                            onClick = { viewModel.onCategoryChange(c) },
                            label = { Text(c.label) },
                            shape = RoundedCornerShape(50),
                        )
                    }
                }
                OutlinedTextField(
                    value = state.location,
                    onValueChange = viewModel::onLocationChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("مکان (اختیاری)") },
                    singleLine = true,
                )
                state.error?.let { Text(it, color = scheme.error, style = MaterialTheme.typography.bodyMedium) }
                Button(
                    onClick = viewModel::save,
                    enabled = !state.isSaving,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = scheme.primaryContainer, contentColor = scheme.onPrimaryContainer),
                ) { Text(if (state.isNew) "افزودن رویداد" else "ذخیره تغییرات", style = MaterialTheme.typography.labelLarge) }
            }
        }
    }

    if (showDate) {
        JalaliDatePickerDialog(
            initial = state.date,
            today = viewModel.today,
            onConfirm = { viewModel.onDateChange(it); showDate = false },
            onDismiss = { showDate = false },
        )
    }
    if (showStart) {
        RoozYarTimePickerDialog(
            initial = state.startTime,
            onConfirm = { viewModel.onStartTimeChange(it); showStart = false },
            onDismiss = { showStart = false },
        )
    }
    if (showEnd) {
        RoozYarTimePickerDialog(
            initial = state.endTime ?: state.startTime,
            onConfirm = { viewModel.onEndTimeChange(it); showEnd = false },
            onDismiss = { showEnd = false },
        )
    }
    if (confirmDelete) {
        ConfirmDialog(
            title = "حذف رویداد",
            message = "این رویداد برای همیشه حذف می‌شود.",
            confirmLabel = "حذف",
            destructive = true,
            onConfirm = { confirmDelete = false; viewModel.delete() },
            onDismiss = { confirmDelete = false },
        )
    }
}
