package com.roozyar.app.core.design

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.roozyar.app.R

val Vazirmatn = FontFamily(
    Font(R.font.vazirmatn_regular, FontWeight.Normal),
    Font(R.font.vazirmatn_medium, FontWeight.Medium),
    Font(R.font.vazirmatn_semibold, FontWeight.SemiBold),
    Font(R.font.vazirmatn_bold, FontWeight.Bold),
)

private fun style(size: Int, line: Int, weight: FontWeight) = TextStyle(
    fontFamily = Vazirmatn,
    fontSize = size.sp,
    lineHeight = line.sp,
    fontWeight = weight,
    letterSpacing = 0.sp, // negative tracking breaks Persian joins (DESIGN.md)
)

/** Sizes and line heights from DESIGN.md (both files); tall leading for Persian. */
internal val RoozYarTypography = Typography(
    displayLarge = style(36, 48, FontWeight.Bold),
    displayMedium = style(30, 40, FontWeight.Bold),
    displaySmall = style(26, 36, FontWeight.Bold),
    headlineLarge = style(26, 36, FontWeight.Bold),
    headlineMedium = style(22, 32, FontWeight.SemiBold),
    headlineSmall = style(20, 28, FontWeight.Bold),
    titleLarge = style(18, 28, FontWeight.SemiBold),
    titleMedium = style(16, 24, FontWeight.Bold),
    titleSmall = style(14, 20, FontWeight.SemiBold),
    bodyLarge = style(16, 26, FontWeight.Normal),
    bodyMedium = style(13, 22, FontWeight.Normal),
    bodySmall = style(12, 18, FontWeight.Normal),
    labelLarge = style(14, 20, FontWeight.SemiBold),
    labelMedium = style(12, 16, FontWeight.Medium),
    labelSmall = style(11, 16, FontWeight.Medium),
)
