package com.roozyar.feature.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.roozyar.core.design.RoozYarSurfaceContainer

@Composable
fun DashboardScreen(contentPadding: PaddingValues = PaddingValues()) {
    Column(
        modifier = Modifier.fillMaxSize().padding(contentPadding).padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("روزیارت رو بساز", style = MaterialTheme.typography.headlineMedium)
        Text("امروز، یک قدم کوچک برای زندگی بهتر.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Card(colors = CardDefaults.cardColors(containerColor = RoozYarSurfaceContainer)) {
            Column(Modifier.padding(18.dp)) {
                Text("خلاصه امروز", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Text("زیرساخت اولیه روزیار آماده توسعه است.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Card(colors = CardDefaults.cardColors(containerColor = RoozYarSurfaceContainer)) {
            Column(Modifier.padding(18.dp)) {
                Text("وظایف", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Text("مدیریت وظایف از داشبورد و برنامه‌ریز در مراحل بعدی فعال می‌شود.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
