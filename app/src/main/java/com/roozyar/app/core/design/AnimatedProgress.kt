package com.roozyar.app.core.design

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/** Linear progress bar that glides to its new value instead of jumping. */
@Composable
fun AnimatedLinearProgress(progress: Float, modifier: Modifier = Modifier, color: Color, trackColor: Color) {
    val animated by animateFloatAsState(progress.coerceIn(0f, 1f), animationSpec = tween(600), label = "progress")
    LinearProgressIndicator(progress = { animated }, modifier = modifier, color = color, trackColor = trackColor)
}
