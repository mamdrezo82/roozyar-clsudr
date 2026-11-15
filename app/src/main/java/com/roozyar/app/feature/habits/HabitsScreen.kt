package com.roozyar.app.feature.habits

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.roozyar.app.core.common.toPersianDigits
import com.roozyar.app.core.design.EmptyState
import com.roozyar.app.core.design.RoozYarCard
import com.roozyar.app.core.design.RoozYarTopBar

@Composable
fun HabitsScreen(
    onBack: () -> Unit,
    onAddHabit: () -> Unit,
    onEditHabit: (Long) -> Unit,
    viewModel: HabitsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val scheme = MaterialTheme.colorScheme

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            RoozYarTopBar("عادت‌ها", onBack = onBack)
            when {
                state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                state.error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(state.error!!, color = scheme.error)
                }
                state.cards.isEmpty() -> EmptyState("هنوز عادتی ندارید", "با دکمه «عادت جدید» اولین عادت را بسازید.")
                else -> LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item(key = "summary") { TodayCard(state) }
                    items(state.cards, key = { it.habit.id }) { card ->
                        HabitCard(
                            card = card,
                            onClick = { onEditHabit(card.habit.id) },
                            onToggle = { viewModel.toggleToday(card.habit, card.todayCount) },
                            onChange = { delta -> viewModel.changeProgress(card.habit, card.todayCount, delta) },
                        )
                    }
                }
            }
        }
        ExtendedFloatingActionButton(
            onClick = onAddHabit,
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
            containerColor = scheme.primaryContainer,
            contentColor = scheme.onPrimaryContainer,
            shape = RoundedCornerShape(50),
            icon = { Icon(Icons.Filled.Add, contentDescription = null) },
            text = { Text("عادت جدید", style = MaterialTheme.typography.labelLarge) },
        )
    }
}

@Composable
private fun TodayCard(state: HabitsUiState) {
    val scheme = MaterialTheme.colorScheme
    RoozYarCard(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { state.progress },
                    modifier = Modifier.size(64.dp),
                    color = scheme.primary,
                    trackColor = scheme.surfaceContainerHighest,
                    strokeWidth = 6.dp,
                )
                Text("٪" + (state.progress * 100).toInt().toPersianDigits(), style = MaterialTheme.typography.labelMedium)
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("عادت‌های امروز", style = MaterialTheme.typography.titleMedium)
                Text(
                    if (state.totalToday == 0) "امروز عادتی برنامه‌ریزی نشده است."
                    else "${state.doneToday.toPersianDigits()} از ${state.totalToday.toPersianDigits()} عادت انجام شده",
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun HabitCard(card: HabitCardState, onClick: () -> Unit, onToggle: () -> Unit, onChange: (Int) -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val habit = card.habit
    RoozYarCard(Modifier.fillMaxWidth(), onClick = onClick) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(shape = MaterialTheme.shapes.medium, color = scheme.primaryContainer.copy(alpha = 0.2f)) {
                    Icon(habitIcon(habit.icon), contentDescription = null, tint = scheme.primary, modifier = Modifier.padding(10.dp).size(24.dp))
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(habit.title, style = MaterialTheme.typography.titleSmall)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(
                            Icons.Outlined.LocalFireDepartment,
                            contentDescription = null,
                            tint = if (card.streak > 0) scheme.secondary else scheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            if (card.streak > 0) "${card.streak.toPersianDigits()} روز پیاپی" else "بدون استریک",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (card.streak > 0) scheme.secondary else scheme.onSurfaceVariant,
                        )
                    }
                }
                when {
                    !card.scheduledToday -> Text("امروز نیست", style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant)
                    habit.targetCount <= 1 -> IconButton(onClick = onToggle) {
                        Icon(
                            if (card.doneToday) Icons.Filled.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                            contentDescription = if (card.doneToday) "لغو انجام" else "انجام شد",
                            tint = if (card.doneToday) scheme.primary else scheme.onSurfaceVariant,
                            modifier = Modifier.size(32.dp),
                        )
                    }
                    else -> Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { onChange(-1) }, enabled = card.todayCount > 0) {
                            Icon(Icons.Filled.Remove, contentDescription = "کم کردن")
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "${card.todayCount.toPersianDigits()}/${habit.targetCount.toPersianDigits()}",
                                style = MaterialTheme.typography.titleSmall,
                                color = if (card.doneToday) scheme.primary else scheme.onSurface,
                            )
                            if (habit.unit.isNotBlank()) {
                                Text(habit.unit, style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant)
                            }
                        }
                        IconButton(onClick = { onChange(1) }) { Icon(Icons.Filled.Add, contentDescription = "افزودن") }
                    }
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                card.week.forEach { day -> WeekDot(day) }
            }
            card.completionPercent?.let {
                Text(
                    "پایبندی ۳۰ روز اخیر: ٪${it.toPersianDigits()}",
                    style = MaterialTheme.typography.labelMedium,
                    color = scheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun WeekDot(day: WeekDay) {
    val scheme = MaterialTheme.colorScheme
    val (fill, content) = when (day.mark) {
        DayMark.DONE -> scheme.primary to scheme.onPrimary
        DayMark.MISSED -> scheme.error.copy(alpha = 0.25f) to scheme.error
        DayMark.PENDING -> scheme.surfaceContainerHighest to scheme.onSurface
        DayMark.OFF -> Color.Transparent to scheme.onSurfaceVariant.copy(alpha = 0.4f)
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(weekdayLetter(day.date.dayOfWeek), style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant)
        Box(Modifier.size(28.dp).clip(CircleShape).background(fill), contentAlignment = Alignment.Center) {
            Text(
                day.date.dayOfMonth.toPersianDigits(),
                style = MaterialTheme.typography.labelSmall,
                color = content,
                textAlign = TextAlign.Center,
            )
        }
    }
}
