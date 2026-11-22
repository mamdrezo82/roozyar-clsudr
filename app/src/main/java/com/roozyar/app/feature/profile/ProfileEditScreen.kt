package com.roozyar.app.feature.profile

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.roozyar.app.core.design.FieldTag
import com.roozyar.app.core.design.FormField
import com.roozyar.app.core.design.FormScreen
import com.roozyar.app.core.design.FormSection
import com.roozyar.app.core.design.RoozYarTopBar
import com.roozyar.app.core.design.SwitchRow

@Composable
fun ProfileEditScreen(onDone: () -> Unit, viewModel: ProfileEditViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val scheme = MaterialTheme.colorScheme

    LaunchedEffect(state.done) { if (state.done) onDone() }

    if (state.isLoading) {
        Column(Modifier.fillMaxSize()) {
            RoozYarTopBar("ویرایش مشخصات", onBack = onDone)
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        }
        return
    }

    val p = state.profile
    FormScreen(
        title = "ویرایش مشخصات",
        onBack = onDone,
        primaryLabel = "ذخیره تغییرات",
        onPrimary = viewModel::save,
        primaryEnabled = !state.isSaving,
    ) {
        FormSection("مشخصات فردی", Icons.Outlined.Person) {
            FormField(
                value = p.displayName,
                onValueChange = viewModel::onName,
                label = "نام",
                tag = FieldTag.Optional,
                placeholder = "نامی که روزیار شما را با آن صدا بزند",
                leadingIcon = Icons.Outlined.Person,
                helper = "در خوشامدگویی صفحه خانه استفاده می‌شود.",
                maxChars = 40,
            )
            FormField(
                value = p.bio,
                onValueChange = viewModel::onBio,
                label = "درباره من",
                tag = FieldTag.Optional,
                placeholder = "یک جمله کوتاه درباره خودتان",
                leadingIcon = Icons.Outlined.Info,
                singleLine = false,
                minLines = 3,
                maxChars = 160,
                helper = "این متن در صفحه پروفایل نمایش داده می‌شود.",
            )
        }

        FormSection("اطلاعات تماس", Icons.Outlined.Email) {
            FormField(
                value = p.email,
                onValueChange = viewModel::onEmail,
                label = "ایمیل",
                tag = FieldTag.Optional,
                placeholder = "name@example.com",
                leadingIcon = Icons.Outlined.Email,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            )
            SwitchRow("نمایش ایمیل در پروفایل", p.showEmail, viewModel::onShowEmail)
            FormField(
                value = p.phone,
                onValueChange = viewModel::onPhone,
                label = "شماره تماس",
                tag = FieldTag.Optional,
                placeholder = "09xxxxxxxxx",
                leadingIcon = Icons.Outlined.Phone,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            )
            SwitchRow("نمایش تلفن در پروفایل", p.showPhone, viewModel::onShowPhone)
            Text(
                "این اطلاعات فقط روی همین گوشی ذخیره می‌شوند و جایی ارسال نمی‌شوند.",
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant,
            )
        }

        state.error?.let { Text(it, color = scheme.error, style = MaterialTheme.typography.bodyMedium) }
    }
}
