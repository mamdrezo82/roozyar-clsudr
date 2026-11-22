package com.roozyar.app.feature.tasks

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.CircularProgressIndicator
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
import com.roozyar.app.core.design.FieldTag
import com.roozyar.app.core.design.FormField
import com.roozyar.app.core.design.FormScreen
import com.roozyar.app.core.design.FormSection
import com.roozyar.app.core.design.JalaliDatePickerDialog
import com.roozyar.app.core.design.PickerField
import com.roozyar.app.core.design.RoozYarTimePickerDialog
import com.roozyar.app.core.design.RoozYarTopBar
import com.roozyar.app.core.notifications.rememberNotificationPermissionRequest
import com.roozyar.app.domain.model.TaskCategory
import com.roozyar.app.domain.model.TaskPriority
import com.roozyar.app.domain.model.TaskRepeat
import com.roozyar.app.domain.reminders.ReminderTimes
import java.util.Locale

@Composable
fun TaskEditScreen(
    onDone: () -> Unit,
    viewModel: TaskEditViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    val requestNotifications = rememberNotificationPermissionRequest()

    LaunchedEffect(state.saved) { if (state.saved) onDone() }

    if (state.isLoading || state.loadError != null) {
        androidx.compose.foundation.layout.Column(Modifier.fillMaxSize()) {
            RoozYarTopBar(if (state.isNew) "وظیفه جدید" else "ویرایش وظیفه", onBack = onDone)
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                if (state.loadError != null) Text(state.loadError!!, color = MaterialTheme.colorScheme.error) else CircularProgressIndicator()
            }
        }
        return
    }

    FormScreen(
        title = if (state.isNew) "وظیفه جدید" else "ویرایش وظیفه",
        onBack = onDone,
        primaryLabel = if (state.isNew) "افزودن وظیفه" else "ذخیره تغییرات",
        onPrimary = viewModel::save,
        primaryEnabled = !state.isSaving,
    ) {
        FormSection("اطلاعات وظیفه", Icons.Outlined.Edit) {
            FormField(
                value = state.title,
                onValueChange = viewModel::onTitleChange,
                label = "عنوان",
                tag = FieldTag.Required,
                placeholder = "مثلاً: ارسال گزارش هفتگی",
                leadingIcon = Icons.Outlined.Edit,
                error = if (state.titleError) "عنوان را وارد کنید." else null,
                maxChars = 120,
            )
            FormField(
                value = state.description,
                onValueChange = viewModel::onDescriptionChange,
                label = "توضیحات",
                tag = FieldTag.Optional,
                placeholder = "جزئیات یا یادداشت کوتاه",
                singleLine = false,
                minLines = 3,
                maxChars = 400,
            )
        }

        FormSection("اولویت و دسته", Icons.Outlined.Category) {
            ChoiceChips(
                options = TaskPriority.entries,
                selected = state.priority,
                onSelect = viewModel::onPriorityChange,
                labelOf = { priorityLabel(it) },
                label = "اولویت",
            )
            ChoiceChips(
                options = TaskCategory.entries,
                selected = state.category,
                onSelect = viewModel::onCategoryChange,
                labelOf = { it.label },
                label = "دسته",
            )
        }

        FormSection("زمان‌بندی", Icons.Outlined.Schedule) {
            val date = state.dueDate
            PickerField(
                label = "مهلت",
                valueText = date?.let { "${it.day.toPersianDigits()} ${JalaliFormatter.monthName(it.month)} ${it.year.toPersianDigits()}" },
                placeholder = "انتخاب تاریخ",
                icon = Icons.Outlined.CalendarMonth,
                tag = FieldTag.Optional,
                onClick = { showDatePicker = true },
                onClear = { viewModel.onDueDateChange(null) },
            )
            if (date != null) {
                val time = state.dueTime
                PickerField(
                    label = "ساعت",
                    valueText = time?.let { String.format(Locale.ROOT, "%02d:%02d", it.hour, it.minute).toPersianDigits() },
                    placeholder = "افزودن ساعت",
                    icon = Icons.Outlined.Schedule,
                    tag = FieldTag.Optional,
                    onClick = { showTimePicker = true },
                    onClear = { viewModel.onDueTimeChange(null) },
                )
                ChoiceChips(
                    options = TaskRepeat.entries,
                    selected = state.repeat,
                    onSelect = viewModel::onRepeatChange,
                    labelOf = { it.label },
                    label = "تکرار",
                )
                ChoiceChips(
                    options = listOf<Int?>(null) + ReminderTimes.taskOffsetsMinutes,
                    selected = state.reminder,
                    onSelect = {
                        viewModel.onReminderChange(it)
                        if (it != null) requestNotifications()
                    },
                    labelOf = { if (it == null) "بدون یادآوری" else ReminderTimes.taskOffsetLabel(it) },
                    label = "یادآوری",
                    helper = (if (time == null) "بدون ساعت، یادآوری نسبت به ساعت ۹ صبح همان روز است. " else "") +
                        "یادآوری‌ها ممکن است کمی با تأخیر برسند.",
                )
            }
            ChoiceChips(
                options = listOf<Int?>(null, 15, 30, 45, 60, 90, 120),
                selected = state.estimate,
                onSelect = viewModel::onEstimateChange,
                labelOf = { if (it == null) "نامشخص" else "${it.toPersianDigits()} دقیقه" },
                label = "برآورد زمان",
                tag = FieldTag.Optional,
                helper = "برنامه‌ریز از این عدد برای چیدن وظیفه در زمان‌های آزاد استفاده می‌کند.",
            )
        }

        if (state.goals.isNotEmpty()) {
            FormSection("هدف مرتبط", Icons.Outlined.Flag) {
                ChoiceChips(
                    options = listOf<Long?>(null) + state.goals.map { it.id },
                    selected = state.goalId,
                    onSelect = viewModel::onGoalChange,
                    labelOf = { id -> if (id == null) "بدون هدف" else state.goals.firstOrNull { it.id == id }?.title ?: "" },
                )
            }
        }

        state.saveError?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium) }
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
