package com.roozyar.app.feature.profile

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.roozyar.app.core.common.toPersianDigits
import com.roozyar.app.core.design.RoozYarCard
import com.roozyar.app.core.design.RoozYarTopBar

@Composable
fun ProfileScreen(onEdit: () -> Unit, viewModel: ProfileViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val scheme = MaterialTheme.colorScheme

    Column(Modifier.fillMaxSize()) {
        RoozYarTopBar("پروفایل و تنظیمات")
        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            state.error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(state.error!!, color = scheme.error) }
            else -> Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                val p = state.profile
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(Modifier.size(88.dp).clip(CircleShape).background(scheme.primaryContainer), contentAlignment = Alignment.Center) {
                        Text(avatarInitial(p.displayName), style = MaterialTheme.typography.displaySmall, color = scheme.onPrimaryContainer)
                    }
                    Text(p.displayName.ifBlank { "کاربر روزیار" }, style = MaterialTheme.typography.headlineSmall)
                    if (p.bio.isNotBlank()) {
                        Text(p.bio, style = MaterialTheme.typography.bodyMedium, color = scheme.onSurfaceVariant, textAlign = TextAlign.Center)
                    }
                    if (p.showEmail && p.email.isNotBlank()) Text(p.email, style = MaterialTheme.typography.labelMedium, color = scheme.onSurfaceVariant)
                    if (p.showPhone && p.phone.isNotBlank()) Text(p.phone.toPersianDigits(), style = MaterialTheme.typography.labelMedium, color = scheme.onSurfaceVariant)
                    Button(
                        onClick = onEdit,
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = scheme.primaryContainer, contentColor = scheme.onPrimaryContainer),
                    ) {
                        Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text("  ویرایش مشخصات", style = MaterialTheme.typography.labelLarge)
                    }
                }

                Text("آمار شما", style = MaterialTheme.typography.titleMedium)
                val s = state.stats
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatTile("وظیفه انجام‌شده", s.tasksDone, scheme.primary, Modifier.weight(1f))
                    StatTile("وظیفه باز", s.tasksOpen, scheme.secondary, Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatTile("عادت فعال", s.habitCount, scheme.primary, Modifier.weight(1f))
                    StatTile("بهترین استریک جاری", s.bestStreak, scheme.secondary, Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatTile("هدف فعال", s.activeGoals, scheme.tertiary, Modifier.weight(1f))
                    StatTile("هدف تکمیل‌شده", s.completedGoals, scheme.primary, Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatTile("یادداشت ژورنال", s.journalEntries, scheme.tertiary, Modifier.weight(1f))
                    Box(Modifier.weight(1f))
                }

                Text("ظاهر", style = MaterialTheme.typography.titleMedium)
                InfoCard("تم تاریک", "رابط برنامه فعلاً فقط در حالت تاریک و راست‌به‌چپ ارائه می‌شود.")

                Text("داده‌ها و حریم خصوصی", style = MaterialTheme.typography.titleMedium)
                InfoCard(
                    "ذخیره‌سازی محلی",
                    "همه وظایف، عادت‌ها، اهداف، یادداشت‌ها و رویدادها فقط روی همین گوشی ذخیره می‌شوند و جایی ارسال نمی‌شوند. " +
                        "با حذف برنامه یا پاک‌کردن داده‌های آن، اطلاعات از بین می‌رود؛ خروجی پشتیبان هنوز ساخته نشده است.",
                )

                Text("درباره", style = MaterialTheme.typography.titleMedium)
                val context = LocalContext.current
                InfoCard("روزیار", "نسخه ${appVersion(context).toPersianDigits()}")
            }
        }
    }
}

private fun appVersion(context: Context): String = try {
    context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "?"
} catch (e: Exception) {
    "?"
}

@Composable
private fun StatTile(label: String, value: Int, color: Color, modifier: Modifier) {
    Surface(modifier = modifier, shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surfaceContainerLow) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(value.toPersianDigits(), style = MaterialTheme.typography.headlineSmall, color = color)
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun InfoCard(title: String, text: String) {
    RoozYarCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
