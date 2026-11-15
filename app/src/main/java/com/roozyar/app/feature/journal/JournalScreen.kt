package com.roozyar.app.feature.journal

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.roozyar.app.core.common.toPersianDigits
import com.roozyar.app.core.design.EmptyState
import com.roozyar.app.core.design.RoozYarCard
import com.roozyar.app.core.design.RoozYarTopBar
import com.roozyar.app.domain.model.JournalEntry

@Composable
fun JournalScreen(
    onBack: () -> Unit,
    onAddEntry: () -> Unit,
    onOpenEntry: (Long) -> Unit,
    viewModel: JournalViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val scheme = MaterialTheme.colorScheme

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            RoozYarTopBar("ژورنال", onBack = onBack)
            when {
                state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                state.error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(state.error!!, color = scheme.error)
                }
                !state.hasAny -> EmptyState("هنوز یادداشتی ندارید", "با دکمه «یادداشت جدید» اولین یادداشت را بنویسید. همه چیز فقط روی گوشی شما می‌ماند.")
                else -> LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item(key = "stats") { StatsCard(state.stats) }
                    item(key = "search") {
                        OutlinedTextField(
                            value = state.query,
                            onValueChange = viewModel::onQueryChange,
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("جست‌وجو در عنوان، متن و برچسب‌ها") },
                            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                            trailingIcon = {
                                if (state.query.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.onQueryChange("") }) {
                                        Icon(Icons.Filled.Close, contentDescription = "پاک‌کردن جست‌وجو")
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(50),
                        )
                    }
                    item(key = "filters") {
                        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            JournalFilter.entries.forEach { f ->
                                FilterChip(
                                    selected = state.filter == f,
                                    onClick = { viewModel.onFilterChange(f) },
                                    label = { Text(f.label) },
                                    shape = RoundedCornerShape(50),
                                )
                            }
                        }
                    }
                    if (state.entries.isEmpty()) {
                        item(key = "none") {
                            Text(
                                "نتیجه‌ای پیدا نشد.",
                                modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                                textAlign = TextAlign.Center,
                                color = scheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                    items(state.entries, key = { it.id }) { entry ->
                        EntryCard(entry, state, onClick = { onOpenEntry(entry.id) }, onStar = { viewModel.toggleStar(entry) })
                    }
                }
            }
        }
        ExtendedFloatingActionButton(
            onClick = onAddEntry,
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
            containerColor = scheme.primaryContainer,
            contentColor = scheme.onPrimaryContainer,
            shape = RoundedCornerShape(50),
            icon = { Icon(Icons.Filled.Add, contentDescription = null) },
            text = { Text("یادداشت جدید", style = MaterialTheme.typography.labelLarge) },
        )
    }
}

@Composable
private fun StatsCard(stats: JournalStats) {
    RoozYarCard(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(vertical = 16.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            Stat("روز استمرار", stats.streakDays, MaterialTheme.colorScheme.secondary)
            Stat("یادداشت", stats.entryCount, MaterialTheme.colorScheme.primary)
            Stat("کلمه این هفته", stats.wordsThisWeek, MaterialTheme.colorScheme.tertiary)
        }
    }
}

@Composable
private fun Stat(label: String, value: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value.toPersianDigits(), style = MaterialTheme.typography.headlineSmall, color = color)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun EntryCard(entry: JournalEntry, state: JournalUiState, onClick: () -> Unit, onStar: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    RoozYarCard(Modifier.fillMaxWidth(), onClick = onClick) {
        Column(Modifier.padding(start = 16.dp, top = 8.dp, end = 4.dp, bottom = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        entry.displayTitle.ifBlank { "بدون عنوان" },
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(formatEntryDate(entry, state.zone), style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant)
                }
                IconButton(onClick = onStar) {
                    Icon(
                        if (entry.starred) Icons.Filled.Star else Icons.Outlined.StarBorder,
                        contentDescription = if (entry.starred) "حذف ستاره" else "ستاره‌دار کردن",
                        tint = if (entry.starred) scheme.secondary else scheme.onSurfaceVariant,
                    )
                }
            }
            if (entry.body.isNotBlank()) {
                Text(
                    entry.body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(end = 12.dp),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                if (entry.isDraft) Pill("پیش‌نویس", scheme.secondary)
                entry.mood?.let { Pill("${it.emoji} ${it.label}", scheme.tertiary) }
                entry.tags.take(3).forEach { Pill("#$it", scheme.primary) }
            }
            Text(
                "${entry.wordCount.toPersianDigits()} کلمه • ${entry.readingMinutes.toPersianDigits()} دقیقه مطالعه",
                style = MaterialTheme.typography.labelSmall,
                color = scheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun Pill(text: String, color: Color) {
    Surface(shape = MaterialTheme.shapes.small, color = color.copy(alpha = 0.15f)) {
        Text(text, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = color)
    }
}
