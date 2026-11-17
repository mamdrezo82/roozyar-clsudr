package com.roozyar.app.feature.hub

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.roozyar.app.core.design.RoozYarCard
import com.roozyar.app.core.design.RoozYarTopBar

private data class HubItem(val title: String, val icon: ImageVector, val available: Boolean, val onClick: () -> Unit = {})

/** Entry point to every feature. Features that are not built yet are shown disabled with «به‌زودی». */
@Composable
fun HubScreen(onOpenTasks: () -> Unit, onOpenHabits: () -> Unit, onOpenJournal: () -> Unit, onOpenFocus: () -> Unit, onOpenAchievements: () -> Unit) {
    val items = listOf(
        HubItem("وظایف", Icons.Outlined.TaskAlt, available = true, onClick = onOpenTasks),
        HubItem("عادت‌ها", Icons.Outlined.Repeat, available = true, onClick = onOpenHabits),
        HubItem("ژورنال", Icons.Outlined.Book, available = true, onClick = onOpenJournal),
        HubItem("خط زمانی", Icons.Outlined.Timeline, available = false),
        HubItem("دستاوردها", Icons.Outlined.EmojiEvents, available = true, onClick = onOpenAchievements),
        HubItem("تمرکز", Icons.Outlined.Timer, available = true, onClick = onOpenFocus),
        HubItem("ویجت‌ها", Icons.Outlined.Widgets, available = false),
        HubItem("دستیار هوشمند", Icons.Outlined.AutoAwesome, available = false),
    )
    Column(Modifier.fillMaxSize()) {
        RoozYarTopBar("برنامه‌ها")
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(items) { item -> HubCard(item) }
        }
    }
}

@Composable
private fun HubCard(item: HubItem) {
    val scheme = MaterialTheme.colorScheme
    val tint = if (item.available) scheme.primary else scheme.onSurfaceVariant.copy(alpha = 0.6f)
    RoozYarCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = if (item.available) item.onClick else null,
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(item.icon, contentDescription = null, tint = tint)
            Text(
                item.title,
                style = MaterialTheme.typography.titleSmall,
                color = if (item.available) scheme.onSurface else scheme.onSurfaceVariant,
            )
            Text(
                if (item.available) "باز کردن" else "به‌زودی",
                style = MaterialTheme.typography.labelSmall,
                color = tint,
            )
        }
    }
}
