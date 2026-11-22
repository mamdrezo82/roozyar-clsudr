package com.roozyar.app.feature.habits

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.roozyar.app.core.common.toPersianDigits
import com.roozyar.app.core.design.ConfirmDialog
import com.roozyar.app.core.design.FieldLabel
import com.roozyar.app.core.design.FieldTag
import com.roozyar.app.core.design.FormField
import com.roozyar.app.core.design.FormScreen
import com.roozyar.app.core.design.FormSection
import com.roozyar.app.core.design.MultiChoiceChips
import com.roozyar.app.core.design.PickerField
import com.roozyar.app.core.design.RoozYarTimePickerDialog
import com.roozyar.app.core.design.RoozYarTopBar
import com.roozyar.app.core.design.StepperRow
import com.roozyar.app.core.notifications.rememberNotificationPermissionRequest
import java.time.LocalTime
import java.util.Locale

@Composable
fun HabitEditScreen(onDone: () -> Unit, viewModel: HabitEditViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var confirmDelete by remember { mutableStateOf(false) }
    var showTime by remember { mutableStateOf(false) }
    val requestNotifications = rememberNotificationPermissionRequest()
    val scheme = MaterialTheme.colorScheme

    LaunchedEffect(state.done) { if (state.done) onDone() }

    if (state.isLoading) {
        Column(Modifier.fillMaxSize()) {
            RoozYarTopBar(if (state.isNew) "عادت جدید" else "ویرایش عادت", onBack = onDone)
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        }
        return
    }

    FormScreen(
        title = if (state.isNew) "عادت جدید" else "ویرایش عادت",
        onBack = onDone,
        primaryLabel = if (state.isNew) "افزودن عادت" else "ذخیره تغییرات",
        onPrimary = viewModel::save,
        primaryEnabled = !state.isSaving,
        topActions = {
            if (!state.isNew) {
                IconButton(onClick = { confirmDelete = true }) { Icon(Icons.Filled.Delete, contentDescription = "حذف عادت", tint = scheme.error) }
            }
        },
    ) {
        FormSection("اطلاعات عادت", Icons.Outlined.Edit) {
            FormField(
                value = state.title,
                onValueChange = viewModel::onTitleChange,
                label = "نام عادت",
                tag = FieldTag.Required,
                placeholder = "مثلاً: نوشیدن آب",
                leadingIcon = Icons.Outlined.Edit,
                error = if (state.titleError) "نام عادت را وارد کنید." else null,
                maxChars = 60,
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FieldLabel("آیکون")
                habitIconKeys.chunked(4).forEach { rowKeys ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        rowKeys.forEach { key ->
                            FilterChip(
                                selected = state.icon == key,
                                onClick = { viewModel.onIconChange(key) },
                                label = { Icon(habitIcon(key), contentDescription = null, modifier = Modifier.size(20.dp)) },
                                shape = RoundedCornerShape(50),
                            )
                        }
                    }
                }
            }
        }

        FormSection("تکرار و هدف", Icons.Outlined.CalendarMonth) {
            MultiChoiceChips(
                options = persianWeek,
                selected = state.weekdays,
                onToggle = viewModel::onWeekdayToggle,
                labelOf = { weekdayLetter(it) },
                label = "روزهای تکرار",
                tag = FieldTag.Required,
                error = if (state.weekdaysError) "حداقل یک روز را انتخاب کنید." else null,
            )
            StepperRow(
                label = "هدف روزانه",
                value = state.targetCount,
                onValueChange = viewModel::onTargetChange,
                range = 1..99,
                suffix = if (state.targetCount == 1) "بار" else "بار در روز",
            )
            if (state.targetCount > 1) {
                FormField(
                    value = state.unit,
                    onValueChange = viewModel::onUnitChange,
                    label = "واحد",
                    tag = FieldTag.Optional,
                    placeholder = "مثلاً: لیوان",
                    maxChars = 20,
                )
            }
        }

        FormSection("یادآوری", Icons.Outlined.Notifications) {
            PickerField(
                label = "یادآوری روزانه",
                valueText = state.reminderMinute?.let { m -> String.format(Locale.ROOT, "%02d:%02d", m / 60, m % 60).toPersianDigits() },
                placeholder = "افزودن یادآوری",
                icon = Icons.Outlined.Schedule,
                tag = FieldTag.Optional,
                onClick = { showTime = true },
                onClear = { viewModel.onReminderChange(null) },
                helper = "فقط در روزهای تکرار و تا وقتی هدف روز کامل نشده یادآوری می‌شود.",
            )
        }

        state.error?.let { Text(it, color = scheme.error, style = MaterialTheme.typography.bodyMedium) }
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
