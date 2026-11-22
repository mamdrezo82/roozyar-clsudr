package com.roozyar.app.feature.goals

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Flag
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

    if (state.isLoading) {
        Column(Modifier.fillMaxSize()) {
            RoozYarTopBar(if (state.isNew) "هدف جدید" else "ویرایش هدف", onBack = onDone)
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        }
        return
    }

    FormScreen(
        title = if (state.isNew) "هدف جدید" else "ویرایش هدف",
        onBack = onDone,
        primaryLabel = if (state.isNew) "افزودن هدف" else "ذخیره تغییرات",
        onPrimary = viewModel::save,
        primaryEnabled = !state.isSaving,
        topActions = {
            if (!state.isNew) {
                IconButton(onClick = { confirmDelete = true }) { Icon(Icons.Filled.Delete, contentDescription = "حذف هدف", tint = scheme.error) }
            }
        },
    ) {
        FormSection("اطلاعات هدف", Icons.Outlined.Flag) {
            FormField(
                value = state.title,
                onValueChange = viewModel::onTitleChange,
                label = "عنوان هدف",
                tag = FieldTag.Required,
                placeholder = "مثلاً: دفاع از پایان‌نامه",
                leadingIcon = Icons.Outlined.Edit,
                error = if (state.titleError) "عنوان را وارد کنید." else null,
                maxChars = 100,
            )
            FormField(
                value = state.description,
                onValueChange = viewModel::onDescriptionChange,
                label = "توضیحات",
                tag = FieldTag.Optional,
                placeholder = "چرا این هدف مهم است؟ معیار موفقیت چیست؟",
                singleLine = false,
                minLines = 3,
                maxChars = 400,
            )
        }

        FormSection("دسته و اولویت", Icons.Outlined.Category) {
            ChoiceChips(
                options = GoalCategory.entries,
                selected = state.category,
                onSelect = viewModel::onCategoryChange,
                labelOf = { it.label },
                label = "دسته",
            )
            ChoiceChips(
                options = TaskPriority.entries,
                selected = state.priority,
                onSelect = viewModel::onPriorityChange,
                labelOf = { priorityLabel(it) },
                label = "اولویت",
            )
        }

        FormSection("مهلت", Icons.Outlined.CalendarMonth) {
            val d = state.deadline
            PickerField(
                label = "مهلت نهایی",
                valueText = d?.let { "${it.day.toPersianDigits()} ${JalaliFormatter.monthName(it.month)} ${it.year.toPersianDigits()}" },
                placeholder = "انتخاب تاریخ",
                icon = Icons.Outlined.CalendarMonth,
                tag = FieldTag.Optional,
                onClick = { showDate = true },
                onClear = { viewModel.onDeadlineChange(null) },
                helper = "مایلستون‌ها را بعد از ساخت هدف، در صفحه جزئیات اضافه می‌کنید.",
            )
        }

        state.error?.let { Text(it, color = scheme.error, style = MaterialTheme.typography.bodyMedium) }
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
