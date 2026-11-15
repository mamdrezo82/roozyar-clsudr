package com.roozyar.app.core.design

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.border
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.roozyar.app.core.common.toPersianDigits
import com.roozyar.app.core.date.JalaliDate
import com.roozyar.app.core.date.JalaliFormatter

private val weekdayHeaders = listOf("ش", "ی", "د", "س", "چ", "پ", "ج") // Saturday first

/** Jalali month-grid date picker. Shown in RTL, so Saturday is the rightmost column. */
@Composable
fun JalaliDatePickerDialog(
    initial: JalaliDate?,
    today: JalaliDate,
    onConfirm: (JalaliDate) -> Unit,
    onDismiss: () -> Unit,
) {
    var selected by remember { mutableStateOf(initial) }
    var year by remember { mutableIntStateOf((initial ?: today).year) }
    var month by remember { mutableIntStateOf((initial ?: today).month) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = MaterialTheme.shapes.extraLarge,
        title = {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = {
                    if (month == 1) { month = 12; year -= 1 } else month -= 1
                }) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "ماه قبل") }
                Text(
                    "${JalaliFormatter.monthName(month)} ${year.toPersianDigits()}",
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = {
                    if (month == 12) { month = 1; year += 1 } else month += 1
                }) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "ماه بعد") }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(Modifier.fillMaxWidth()) {
                    weekdayHeaders.forEach { h ->
                        Text(
                            h,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                val first = JalaliDate(year, month, 1)
                val offset = (first.dayOfWeek.value + 1) % 7
                val length = JalaliDate.monthLength(year, month)
                val rows = (offset + length + 6) / 7
                for (r in 0 until rows) {
                    Row(Modifier.fillMaxWidth()) {
                        for (c in 0 until 7) {
                            val day = r * 7 + c - offset + 1
                            if (day in 1..length) {
                                val date = JalaliDate(year, month, day)
                                DayCell(
                                    day = day,
                                    isSelected = date == selected,
                                    isToday = date == today,
                                    modifier = Modifier.weight(1f),
                                    onClick = { selected = date },
                                )
                            } else {
                                Box(Modifier.weight(1f))
                            }
                        }
                    }
                }
                TextButton(onClick = {
                    selected = today
                    year = today.year
                    month = today.month
                }) { Text("امروز") }
            }
        },
        confirmButton = {
            TextButton(enabled = selected != null, onClick = { selected?.let(onConfirm) }) { Text("تأیید") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("انصراف") } },
    )
}

@Composable
private fun DayCell(day: Int, isSelected: Boolean, isToday: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(CircleShape)
            .background(if (isSelected) scheme.primaryContainer else androidx.compose.ui.graphics.Color.Transparent)
            .then(if (isToday && !isSelected) Modifier.border(BorderStroke(1.dp, scheme.primary), CircleShape) else Modifier)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            day.toPersianDigits(),
            style = MaterialTheme.typography.bodyMedium,
            color = if (isSelected) scheme.onPrimaryContainer else scheme.onSurface,
        )
    }
}
