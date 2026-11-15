package com.roozyar.app.feature.journal

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.roozyar.app.core.design.ConfirmDialog
import com.roozyar.app.core.design.RoozYarTopBar
import com.roozyar.app.domain.model.JournalMood

@Composable
fun JournalEditScreen(onDone: () -> Unit, viewModel: JournalEditViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var confirmDelete by remember { mutableStateOf(false) }
    val scheme = MaterialTheme.colorScheme

    LaunchedEffect(state.done) { if (state.done) onDone() }

    Column(Modifier.fillMaxSize()) {
        RoozYarTopBar(
            title = if (state.isNew) "یادداشت جدید" else "ویرایش یادداشت",
            onBack = onDone,
            actions = {
                if (!state.isLoading) {
                    IconButton(onClick = viewModel::onStarToggle) {
                        Icon(
                            if (state.starred) Icons.Filled.Star else Icons.Outlined.StarBorder,
                            contentDescription = "ستاره",
                            tint = if (state.starred) scheme.secondary else scheme.onSurfaceVariant,
                        )
                    }
                    if (!state.isNew) {
                        IconButton(onClick = { confirmDelete = true }) {
                            Icon(Icons.Filled.Delete, contentDescription = "حذف یادداشت", tint = scheme.error)
                        }
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
                    label = { Text("عنوان (اختیاری)") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = state.body,
                    onValueChange = viewModel::onBodyChange,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 200.dp),
                    label = { Text("متن یادداشت") },
                    isError = state.contentError,
                    supportingText = if (state.contentError) ({ Text("عنوان یا متن را بنویسید.") }) else null,
                )
                Text("حال‌وهوا", style = MaterialTheme.typography.titleSmall)
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    JournalMood.entries.forEach { m ->
                        FilterChip(
                            selected = state.mood == m,
                            onClick = { viewModel.onMoodChange(m) },
                            label = { Text("${m.emoji} ${m.label}") },
                            shape = RoundedCornerShape(50),
                        )
                    }
                }
                OutlinedTextField(
                    value = state.tagsText,
                    onValueChange = viewModel::onTagsChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("برچسب‌ها (با ویرگول جدا کنید)") },
                    singleLine = true,
                )
                state.error?.let { Text(it, color = scheme.error, style = MaterialTheme.typography.bodyMedium) }
                Button(
                    onClick = { viewModel.save(asDraft = false) },
                    enabled = !state.isSaving,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = scheme.primaryContainer, contentColor = scheme.onPrimaryContainer),
                ) { Text("ذخیره", style = MaterialTheme.typography.labelLarge) }
                OutlinedButton(
                    onClick = { viewModel.save(asDraft = true) },
                    enabled = !state.isSaving,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(50),
                ) { Text("ذخیره به‌عنوان پیش‌نویس", style = MaterialTheme.typography.labelLarge) }
            }
        }
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
