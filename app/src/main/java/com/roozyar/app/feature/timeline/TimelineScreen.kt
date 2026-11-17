package com.roozyar.app.feature.timeline

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.roozyar.app.core.common.toPersianDigits
import com.roozyar.app.core.date.JalaliFormatter
import com.roozyar.app.core.date.toJalali
import com.roozyar.app.core.design.EmptyState
import com.roozyar.app.core.design.RoozYarCard
import com.roozyar.app.core.design.RoozYarTopBar
import com.roozyar.app.domain.model.TimelineItem
import com.roozyar.app.domain.model.TimelineType
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale

private fun icon(type: TimelineType): ImageVector = when (type) {
    TimelineType.TASK -> Icons.Outlined.TaskAlt
    TimelineType.GOAL -> Icons.Outlined.Flag
    TimelineType.JOURNAL -> Icons.Outlined.Book
    TimelineType.FOCUS -> Icons.Outlined.Timer
    TimelineType.ACHIEVEMENT -> Icons.Outlined.EmojiEvents
    TimelineType.EVENT -> Icons.Outlined.Event
}

@Composable
fun TimelineScreen(onBack: () -> Unit, viewModel: TimelineViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val scheme = MaterialTheme.colorScheme

    Column(Modifier.fillMaxSize()) {
        RoozYarTopBar("خط زمانی زندگی", onBack = onBack)
        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            state.error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(state.error!!, color = scheme.error) }
            !state.hasAny -> EmptyState("هنوز چیزی ثبت نشده", "وظایف انجام‌شده، یادداشت‌ها، جلسه‌های تمرکز، اهداف و دستاوردهای شما اینجا به ترتیب زمان نمایش داده می‌شوند.")
            else -> LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item(key = "filters") {
                    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = state.filter == null, onClick = { viewModel.onFilter(null) }, label = { Text("همه") }, shape = RoundedCornerShape(50))
                        TimelineType.entries.forEach { t ->
                            FilterChip(selected = state.filter == t, onClick = { viewModel.onFilter(t) }, label = { Text(t.label) }, shape = RoundedCornerShape(50))
                        }
                    }
                }
                if (state.days.isEmpty()) {
                    item(key = "none") {
                        Text("در این دسته چیزی نیست.", color = scheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 24.dp), style = MaterialTheme.typography.bodyMedium)
                    }
                }
                state.days.forEach { (date, items) ->
                    item(key = "h$date") { DayHeader(date) }
                    items(items.size, key = { i -> "$date-$i-${items[i].at.toEpochMilli()}-${items[i].type}" }) { i ->
                        Row(item = items[i], zone = state.zone)
                    }
                }
            }
        }
    }
}

@Composable
private fun DayHeader(date: LocalDate) {
    val j = date.toJalali()
    Text(
        "${JalaliFormatter.weekdayName(date.dayOfWeek)}، ${j.day.toPersianDigits()} ${JalaliFormatter.monthName(j.month)} ${j.year.toPersianDigits()}",
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 12.dp, bottom = 2.dp),
    )
}

@Composable
private fun Row(item: TimelineItem, zone: ZoneId) {
    val scheme = MaterialTheme.colorScheme
    val t = item.at.atZone(zone)
    RoozYarCard(Modifier.fillMaxWidth()) {
        androidx.compose.foundation.layout.Row(
            Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(icon(item.type), contentDescription = item.type.label, tint = scheme.primary, modifier = Modifier.size(24.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(item.title, style = MaterialTheme.typography.titleSmall)
                item.subtitle?.let { Text(it.toPersianDigits(), style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant) }
            }
            Text(String.format(Locale.ROOT, "%02d:%02d", t.hour, t.minute).toPersianDigits(), style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant)
        }
    }
}
