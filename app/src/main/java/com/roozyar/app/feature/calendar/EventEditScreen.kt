package com.roozyar.app.feature.calendar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.roozyar.app.core.common.toPersianDigits
import com.roozyar.app.core.date.JalaliFormatter
import com.roozyar.app.core.design.ChoiceChips
import com.roozyar.app.core.design.ConfirmDialog
import com.roozyar.app.core.design.FieldTag
import com.roozyar.app.core.design.FormField
import com.roozyar.app.core.design.FormScreen
import com.roozyar.app.core.design.FormSection
import com.roozyar.app.core.design.JalaliDatePickerDialog
import com.roozyar.app.core.design.PickerField
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

    if (state.isLoading) {
        Column(Modifier.fillMaxSize()) {
            RoozYarTopBar(if (state.isNew) "رویداد جدید" else "ویرایش رویداد", onBack = onDone)
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        }
        return
    }

    FormScreen(
        title = if (state.isNew) "رویداد جدید" else "ویرایش رویداد",
        onBack = onDone,
        primaryLabel = if (state.isNew) "افزودن رویداد" else "ذخیره تغییرات",
        onPrimary = viewModel::save,
        primaryEnabled = !state.isSaving,
        topActions = {
            if (!state.isNew) {
                IconButton(onClick = { confirmDelete = true }) { Icon(Icons.Filled.Delete, contentDescription = "حذف رویداد", tint = scheme.error) }
            }
        },
    ) {
        FormSection("اطلاعات رویداد", Icons.Outlined.Edit) {
            FormField(
                value = state.title,
                onValueChange = viewModel::onTitleChange,
                label = "عنوان",
                tag = FieldTag.Required,
                placeholder = "مثلاً: جلسه با سرپرست",
                leadingIcon = Icons.Outlined.Edit,
                error = if (state.titleError) "عنوان را وارد کنید." else null,
                maxChars = 100,
            )
            FormField(
                value = state.location,
                onValueChange = viewModel::onLocationChange,
                label = "مکان",
                tag = FieldTag.Optional,
                placeholder = "دفتر، آنلاین، ...",
                leadingIcon = Icons.Outlined.Place,
                maxChars = 80,
            )
        }

        FormSection("زمان", Icons.Outlined.Schedule) {
            PickerField(
                label = "تاریخ",
                valueText = "${state.date.day.toPersianDigits()} ${JalaliFormatter.monthName(state.date.month)} ${state.date.year.toPersianDigits()}",
                placeholder = "",
                icon = Icons.Outlined.CalendarMonth,
                tag = FieldTag.Required,
                onClick = { showDate = true },
            )
            PickerField(
                label = "ساعت شروع",
                valueText = state.startTime?.label(),
                placeholder = "افزودن ساعت (بدون ساعت یعنی تمام روز)",
                icon = Icons.Outlined.Schedule,
                tag = FieldTag.Optional,
                onClick = { showStart = true },
                onClear = { viewModel.onStartTimeChange(null) },
            )
            if (state.startTime != null) {
                PickerField(
                    label = "ساعت پایان",
                    valueText = state.endTime?.label(),
                    placeholder = "افزودن ساعت پایان",
                    icon = Icons.Outlined.Schedule,
                    tag = FieldTag.Optional,
                    onClick = { showEnd = true },
                    onClear = { viewModel.onEndTimeChange(null) },
                    error = if (state.timeError) "ساعت پایان باید بعد از شروع باشد." else null,
                )
            }
        }

        FormSection("دسته", Icons.Outlined.Category) {
            ChoiceChips(
                options = EventCategory.entries,
                selected = state.category,
                onSelect = viewModel::onCategoryChange,
                labelOf = { it.label },
            )
        }

        state.error?.let { Text(it, color = scheme.error, style = MaterialTheme.typography.bodyMedium) }
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
