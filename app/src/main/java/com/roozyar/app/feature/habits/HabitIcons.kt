package com.roozyar.app.feature.habits

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.DirectionsRun
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.ui.graphics.vector.ImageVector

/** Habit icons are stored by key so the database never depends on UI classes. */
val habitIconKeys = listOf("star", "water", "run", "book", "meditate", "sun", "sleep", "heart")

fun habitIcon(key: String): ImageVector = when (key) {
    "water" -> Icons.Outlined.WaterDrop
    "run" -> Icons.Outlined.DirectionsRun
    "book" -> Icons.Outlined.MenuBook
    "meditate" -> Icons.Outlined.SelfImprovement
    "sun" -> Icons.Outlined.WbSunny
    "sleep" -> Icons.Outlined.Bedtime
    "heart" -> Icons.Outlined.Favorite
    else -> Icons.Outlined.Star
}
