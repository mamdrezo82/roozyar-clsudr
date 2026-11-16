package com.roozyar.app.feature.profile

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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.roozyar.app.core.design.RoozYarTopBar

@Composable
fun ProfileEditScreen(onDone: () -> Unit, viewModel: ProfileEditViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val scheme = MaterialTheme.colorScheme

    LaunchedEffect(state.done) { if (state.done) onDone() }

    Column(Modifier.fillMaxSize()) {
        RoozYarTopBar("ویرایش مشخصات", onBack = onDone)
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            val p = state.profile
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                OutlinedTextField(value = p.displayName, onValueChange = viewModel::onName, modifier = Modifier.fillMaxWidth(), label = { Text("نام") }, singleLine = true)
                OutlinedTextField(value = p.bio, onValueChange = viewModel::onBio, modifier = Modifier.fillMaxWidth().height(110.dp), label = { Text("درباره من (اختیاری)") })
                OutlinedTextField(
                    value = p.email,
                    onValueChange = viewModel::onEmail,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("ایمیل (اختیاری)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("نمایش ایمیل در پروفایل", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                    Switch(checked = p.showEmail, onCheckedChange = viewModel::onShowEmail)
                }
                OutlinedTextField(
                    value = p.phone,
                    onValueChange = viewModel::onPhone,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("تلفن (اختیاری)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("نمایش تلفن در پروفایل", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                    Switch(checked = p.showPhone, onCheckedChange = viewModel::onShowPhone)
                }
                Text(
                    "این اطلاعات فقط روی همین گوشی ذخیره می‌شوند.",
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant,
                )
                state.error?.let { Text(it, color = scheme.error, style = MaterialTheme.typography.bodyMedium) }
                Button(
                    onClick = viewModel::save,
                    enabled = !state.isSaving,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = scheme.primaryContainer, contentColor = scheme.onPrimaryContainer),
                ) { Text("ذخیره", style = MaterialTheme.typography.labelLarge) }
            }
        }
    }
}
