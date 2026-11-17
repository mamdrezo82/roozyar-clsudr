package com.roozyar.app.feature.report

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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.roozyar.app.core.common.toPersianDigits
import com.roozyar.app.core.design.RoozYarCard
import com.roozyar.app.core.design.RoozYarTopBar
import com.roozyar.app.domain.model.PeriodReport
import com.roozyar.app.domain.model.ReportPeriod

@Composable
fun ReportScreen(onBack: () -> Unit, viewModel: ReportViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val scheme = MaterialTheme.colorScheme

    Column(Modifier.fillMaxSize()) {
        RoozYarTopBar("گزارش‌ها", onBack = onBack)
        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            state.error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(state.error!!, color = scheme.error) }
            else -> Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ReportPeriod.entries.forEach { p ->
                        FilterChip(selected = state.period == p, onClick = { viewModel.onPeriod(p) }, label = { Text(p.label) }, shape = RoundedCornerShape(50))
                    }
                }
                state.report?.let { Content(it) }
            }
        }
    }
}

@Composable
private fun Content(r: PeriodReport) {
    val scheme = MaterialTheme.colorScheme
    val c = r.current
    val p = r.previous
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Metric("وظیفه انجام‌شده", c.tasksDone.toPersianDigits(), delta(c.tasksDone - p.tasksDone, ""), Modifier.weight(1f))
        Metric("دقیقه تمرکز", c.focusMinutes.toPersianDigits(), delta(c.focusMinutes - p.focusMinutes, ""), Modifier.weight(1f))
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Metric("یادداشت", c.journalEntries.toPersianDigits(), delta(c.journalEntries - p.journalEntries, ""), Modifier.weight(1f))
        Metric(
            "پایبندی عادت‌ها",
            c.habitAdherence?.let { "٪" + it.toPersianDigits() } ?: "—",
            if (c.habitAdherence != null && p.habitAdherence != null) delta(c.habitAdherence!! - p.habitAdherence!!, "٪") else null,
            Modifier.weight(1f),
        )
    }
    Text("مقایسه با ${prevLabel(r.period)}", style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant)
    RoozYarCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("نکته‌ها", style = MaterialTheme.typography.titleSmall)
            r.insights.forEach { Text("• " + it.toPersianDigits(), style = MaterialTheme.typography.bodyMedium) }
        }
    }
    if (r.habits.isNotEmpty()) {
        Text("پایبندی هر عادت", style = MaterialTheme.typography.titleMedium)
        r.habits.forEach { h ->
            RoozYarCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row {
                        Text(h.title, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                        Text("٪" + h.percent.toPersianDigits(), style = MaterialTheme.typography.labelLarge, color = scheme.primary)
                    }
                    LinearProgressIndicator(
                        progress = { h.percent / 100f },
                        modifier = Modifier.fillMaxWidth().height(6.dp),
                        color = scheme.primary,
                        trackColor = scheme.surfaceContainerHighest,
                    )
                }
            }
        }
    }
}

private fun prevLabel(p: ReportPeriod) = when (p) {
    ReportPeriod.DAY -> "دیروز"
    ReportPeriod.WEEK -> "هفته قبل"
    ReportPeriod.MONTH -> "۳۰ روز قبل"
}

private data class Delta(val text: String, val positive: Boolean?)

private fun delta(diff: Int, suffix: String): Delta = when {
    diff > 0 -> Delta("+" + diff.toPersianDigits() + suffix, true)
    diff < 0 -> Delta("−" + (-diff).toPersianDigits() + suffix, false)
    else -> Delta("بدون تغییر", null)
}

@Composable
private fun Metric(label: String, value: String, d: Delta?, modifier: Modifier) {
    val scheme = MaterialTheme.colorScheme
    Surface(modifier = modifier, shape = MaterialTheme.shapes.large, color = scheme.surfaceContainerLow) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(value, style = MaterialTheme.typography.headlineSmall, color = scheme.primary)
            Text(label, style = MaterialTheme.typography.labelMedium, color = scheme.onSurfaceVariant)
            if (d != null) {
                val color: Color = when (d.positive) {
                    true -> scheme.primary
                    false -> scheme.error
                    null -> scheme.onSurfaceVariant
                }
                Text(d.text, style = MaterialTheme.typography.labelSmall, color = color)
            }
        }
    }
}
