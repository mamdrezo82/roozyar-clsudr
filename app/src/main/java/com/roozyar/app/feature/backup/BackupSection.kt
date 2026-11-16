package com.roozyar.app.feature.backup

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.roozyar.app.core.common.toPersianDigits
import com.roozyar.app.core.design.ConfirmDialog
import com.roozyar.app.core.design.RoozYarCard

/** Backup and restore card for the Profile screen. Files go wherever the user picks; nothing is uploaded. */
@Composable
fun BackupSection(viewModel: BackupViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val scheme = MaterialTheme.colorScheme

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) viewModel.exportTo(uri)
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) viewModel.onImportPicked(uri)
    }

    RoozYarCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("پشتیبان‌گیری", style = MaterialTheme.typography.titleSmall)
            Text(
                "همه اطلاعات فقط روی همین گوشی است. برای جلوگیری از از دست رفتن داده‌ها (مثلاً با حذف برنامه) یک فایل پشتیبان بگیرید. فایل رمزنگاری نمی‌شود.",
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { exportLauncher.launch(viewModel.suggestedFileName()) },
                    enabled = !state.busy,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = scheme.primaryContainer, contentColor = scheme.onPrimaryContainer),
                ) { Text("تهیه پشتیبان", style = MaterialTheme.typography.labelLarge) }
                OutlinedButton(
                    onClick = { importLauncher.launch(arrayOf("*/*")) },
                    enabled = !state.busy,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(50),
                ) { Text("بازیابی از فایل", style = MaterialTheme.typography.labelLarge) }
            }
            state.message?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = if (state.isError) scheme.error else scheme.primary)
            }
        }
    }

    state.pendingCounts?.let { c ->
        ConfirmDialog(
            title = "بازیابی اطلاعات",
            message = "تمام اطلاعات فعلی حذف و با این فایل جایگزین می‌شود:\n" +
                "${c.tasks.toPersianDigits()} وظیفه، ${c.habits.toPersianDigits()} عادت، ${c.goals.toPersianDigits()} هدف، " +
                "${c.journalEntries.toPersianDigits()} یادداشت، ${c.events.toPersianDigits()} رویداد.\nاین کار قابل بازگشت نیست.",
            confirmLabel = "جایگزین کن",
            destructive = true,
            onConfirm = viewModel::confirmImport,
            onDismiss = viewModel::cancelImport,
        )
    }
}
