package com.roozyar.app.feature.achievements

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.roozyar.app.core.design.AnimatedLinearProgress
import com.roozyar.app.core.common.toPersianDigits
import com.roozyar.app.core.date.JalaliFormatter
import com.roozyar.app.core.date.toJalali
import com.roozyar.app.core.design.RoozYarCard
import com.roozyar.app.core.design.RoozYarTopBar
import com.roozyar.app.domain.model.LevelProgress
import com.roozyar.app.domain.model.Xp
import java.time.ZoneId

@Composable
fun AchievementsScreen(onBack: () -> Unit, viewModel: AchievementsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val scheme = MaterialTheme.colorScheme
    val zone = ZoneId.systemDefault()

    Column(Modifier.fillMaxSize()) {
        RoozYarTopBar("دستاوردها و امتیاز", onBack = onBack)
        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            state.error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(state.error!!, color = scheme.error) }
            else -> Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                LevelCard(state.level)
                RoozYarCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("امتیاز چگونه به دست می‌آید؟", style = MaterialTheme.typography.titleSmall)
                        Text(
                            "وظیفه انجام‌شده ${Xp.PER_TASK.toPersianDigits()} (فوری ${(Xp.PER_TASK + Xp.PER_CRITICAL_TASK_BONUS).toPersianDigits()}) • " +
                                "روز تکمیل عادت ${Xp.PER_HABIT_DAY.toPersianDigits()} • یادداشت ${Xp.PER_JOURNAL_ENTRY.toPersianDigits()} • " +
                                "هر ${Xp.FOCUS_BLOCK_MINUTES.toPersianDigits()} دقیقه تمرکز ${Xp.PER_FOCUS_BLOCK.toPersianDigits()} • " +
                                "مایلستون ${Xp.PER_MILESTONE.toPersianDigits()} • هدف تکمیل‌شده ${Xp.PER_GOAL.toPersianDigits()}. " +
                                "امتیاز از روی اطلاعات ذخیره‌شده محاسبه می‌شود؛ با حذف یک مورد، امتیازش هم کم می‌شود.",
                            style = MaterialTheme.typography.bodySmall,
                            color = scheme.onSurfaceVariant,
                        )
                    }
                }
                Text(
                    "دستاوردها (${state.unlockedCount.toPersianDigits()} از ${state.items.size.toPersianDigits()})",
                    style = MaterialTheme.typography.titleMedium,
                )
                state.items.forEach { item ->
                    RoozYarCard(Modifier.fillMaxWidth()) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Icon(
                                if (item.unlocked) Icons.Filled.EmojiEvents else Icons.Outlined.Lock,
                                contentDescription = null,
                                tint = if (item.unlocked) scheme.secondary else scheme.onSurfaceVariant,
                                modifier = Modifier.size(32.dp),
                            )
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(item.def.title, style = MaterialTheme.typography.titleSmall)
                                Text(item.def.description, style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant)
                                val at = item.unlockedAt
                                if (at != null) {
                                    val j = at.atZone(zone).toLocalDate().toJalali()
                                    Text(
                                        "کسب‌شده در ${j.day.toPersianDigits()} ${JalaliFormatter.monthName(j.month)} ${j.year.toPersianDigits()}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = scheme.primary,
                                    )
                                } else {
                                    AnimatedLinearProgress(
                                        progress = item.fraction,
                                        modifier = Modifier.fillMaxWidth().height(6.dp),
                                        color = scheme.secondary,
                                        trackColor = scheme.surfaceContainerHighest,
                                    )
                                    Text(
                                        "${item.value.toPersianDigits()} از ${item.def.target.toPersianDigits()}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = scheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun LevelCard(level: LevelProgress, onClick: (() -> Unit)? = null) {
    val scheme = MaterialTheme.colorScheme
    RoozYarCard(Modifier.fillMaxWidth(), onClick = onClick) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("سطح ${level.level.toPersianDigits()}", style = MaterialTheme.typography.headlineSmall, color = scheme.secondary, modifier = Modifier.weight(1f))
                Text("${level.totalXp.toPersianDigits()} امتیاز", style = MaterialTheme.typography.titleMedium, color = scheme.primary)
            }
            AnimatedLinearProgress(
                progress = level.fraction,
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = scheme.primary,
                trackColor = scheme.surfaceContainerHighest,
            )
            Text(
                "${(level.xpForNext - level.xpIntoLevel).toPersianDigits()} امتیاز تا سطح ${(level.level + 1).toPersianDigits()}",
                style = MaterialTheme.typography.labelMedium,
                color = scheme.onSurfaceVariant,
            )
        }
    }
}

/** Compact level summary for other screens; tapping opens the full achievements screen. */
@Composable
fun LevelSummaryCard(onClick: () -> Unit, viewModel: AchievementsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    if (!state.isLoading && state.error == null) LevelCard(state.level, onClick)
}
