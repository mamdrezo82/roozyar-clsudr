package com.roozyar.app.feature.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.roozyar.app.core.design.RoozYarCard
import com.roozyar.app.core.design.RoozYarTopBar
import com.roozyar.app.domain.model.DashboardMode

private fun modeHint(mode: DashboardMode) = when (mode) {
    DashboardMode.GENERAL -> "پیشرفت، رویدادها، وظایف، عادت‌ها و اهداف."
    DashboardMode.EXAM -> "تمرکز و وظایف درسی در بالا، سپس اهداف، عادت‌ها و سطح."
    DashboardMode.INTERNSHIP -> "رویدادها و وظایف کاری در بالا، سپس پیشرفت، اهداف و تمرکز."
}

@Composable
fun DashboardCustomizeScreen(onBack: () -> Unit, viewModel: DashboardCustomizeViewModel = hiltViewModel()) {
    val layout by viewModel.layout.collectAsStateWithLifecycle()
    val scheme = MaterialTheme.colorScheme

    Column(Modifier.fillMaxSize()) {
        RoozYarTopBar("سفارشی‌سازی صفحه خانه", onBack = onBack)
        val l = layout
        if (l == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text("حالت", style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DashboardMode.entries.forEach { m ->
                        FilterChip(selected = l.mode == m, onClick = { viewModel.setMode(m) }, label = { Text(m.label) }, shape = RoundedCornerShape(50))
                    }
                }
                Text(
                    modeHint(l.mode) + " انتخاب حالت، چیدمان فعلی را با چیدمان آماده آن حالت جایگزین می‌کند.",
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant,
                )
                Text("کارت‌ها", style = MaterialTheme.typography.titleMedium)
                l.order.forEachIndexed { index, card ->
                    RoozYarCard(Modifier.fillMaxWidth()) {
                        Row(Modifier.padding(start = 16.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(card.label, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                            IconButton(onClick = { viewModel.move(card, -1) }, enabled = index > 0) {
                                Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "بالا")
                            }
                            IconButton(onClick = { viewModel.move(card, 1) }, enabled = index < l.order.lastIndex) {
                                Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "پایین")
                            }
                            Switch(checked = card !in l.hidden, onCheckedChange = { viewModel.toggle(card) }, modifier = Modifier.padding(horizontal = 8.dp))
                        }
                    }
                }
                OutlinedButton(onClick = viewModel::resetToMode, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(50)) {
                    Text("بازنشانی به چیدمان آماده «${l.mode.label}»")
                }
            }
        }
    }
}
