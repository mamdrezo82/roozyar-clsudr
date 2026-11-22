package com.roozyar.app.feature.journal

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.EmojiEmotions
import androidx.compose.material.icons.outlined.Label
import androidx.compose.material.icons.outlined.StarBorder
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
import com.roozyar.app.core.design.ChoiceChips
import com.roozyar.app.core.design.ConfirmDialog
import com.roozyar.app.core.design.FieldTag
import com.roozyar.app.core.design.FormField
import com.roozyar.app.core.design.FormScreen
import com.roozyar.app.core.design.FormSection
import com.roozyar.app.core.design.RoozYarTopBar
import com.roozyar.app.domain.model.JournalMood
import com.roozyar.app.domain.model.countWords

@Composable
fun JournalEditScreen(onDone: () -> Unit, viewModel: JournalEditViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var confirmDelete by remember { mutableStateOf(false) }
    val scheme = MaterialTheme.colorScheme

    LaunchedEffect(state.done) { if (state.done) onDone() }

    if (state.isLoading) {
        Column(Modifier.fillMaxSize()) {
            RoozYarTopBar(if (state.isNew) "یادداشت جدید" else "ویرایش یادداشت", onBack = onDone)
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        }
        return
    }

    FormScreen(
        title = if (state.isNew) "یادداشت جدید" else "ویرایش یادداشت",
        onBack = onDone,
        primaryLabel = "ذخیره",
        onPrimary = { viewModel.save(asDraft = false) },
        primaryEnabled = !state.isSaving,
        secondaryLabel = "پیش‌نویس",
        onSecondary = { viewModel.save(asDraft = true) },
        topActions = {
            IconButton(onClick = viewModel::onStarToggle) {
                Icon(
                    if (state.starred) Icons.Filled.Star else Icons.Outlined.StarBorder,
                    contentDescription = if (state.starred) "حذف ستاره" else "ستاره‌دار کردن",
                    tint = if (state.starred) scheme.secondary else scheme.onSurfaceVariant,
                )
            }
            if (!state.isNew) {
                IconButton(onClick = { confirmDelete = true }) { Icon(Icons.Filled.Delete, contentDescription = "حذف یادداشت", tint = scheme.error) }
            }
        },
    ) {
        FormSection("نوشته", Icons.Outlined.Edit) {
            FormField(
                value = state.title,
                onValueChange = viewModel::onTitleChange,
                label = "عنوان",
                tag = FieldTag.Optional,
                placeholder = "عنوان کوتاه",
                maxChars = 100,
            )
            val words = countWords(state.body)
            FormField(
                value = state.body,
                onValueChange = viewModel::onBodyChange,
                label = "متن یادداشت",
                placeholder = "امروز چه گذشت؟ چه حسی داری؟",
                singleLine = false,
                minLines = 8,
                error = if (state.contentError) "عنوان یا متن را بنویسید." else null,
                helper = if (words == 0) "همه چیز فقط روی گوشی شما می‌ماند." else "${words.toPersianDigits()} واژه • ${maxOf(1, (words + 199) / 200).toPersianDigits()} دقیقه مطالعه",
            )
        }

        FormSection("حال‌وهوا", Icons.Outlined.EmojiEmotions) {
            ChoiceChips(
                options = listOf<JournalMood?>(null) + JournalMood.entries,
                selected = state.mood,
                onSelect = viewModel::onMoodChange,
                labelOf = { if (it == null) "بدون حال‌وهوا" else "${it.emoji} ${it.label}" },
            )
        }

        FormSection("برچسب‌ها", Icons.Outlined.Label) {
            FormField(
                value = state.tagsText,
                onValueChange = viewModel::onTagsChange,
                label = "برچسب‌ها",
                tag = FieldTag.Optional,
                placeholder = "کار، ایده، سلامت",
                helper = "با ویرگول از هم جدا کنید.",
            )
        }

        state.error?.let { Text(it, color = scheme.error, style = MaterialTheme.typography.bodyMedium) }
    }

    if (confirmDelete) {
        ConfirmDialog(
            title = "حذف یادداشت",
            message = "این یادداشت برای همیشه حذف می‌شود.",
            confirmLabel = "حذف",
            destructive = true,
            onConfirm = { confirmDelete = false; viewModel.delete() },
            onDismiss = { confirmDelete = false },
        )
    }
}
