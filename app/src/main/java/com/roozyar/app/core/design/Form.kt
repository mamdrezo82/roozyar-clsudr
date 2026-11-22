package com.roozyar.app.core.design

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.roozyar.app.core.common.toPersianDigits

enum class FieldTag(val label: String?) {
    None(null),
    Required("ضروری"),
    Optional("اختیاری"),
}

/**
 * Standard shell for every create/edit screen: title bar, a scrolling body of [FormSection]s, and a bar at the
 * bottom with the main action (and an optional second one) so saving is always one thumb-tap away.
 */
@Composable
fun FormScreen(
    title: String,
    onBack: () -> Unit,
    primaryLabel: String,
    onPrimary: () -> Unit,
    modifier: Modifier = Modifier,
    primaryEnabled: Boolean = true,
    secondaryLabel: String? = null,
    onSecondary: (() -> Unit)? = null,
    topActions: @Composable () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    Column(modifier.fillMaxSize().imePadding()) {
        RoozYarTopBar(title = title, onBack = onBack, actions = topActions)
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            content()
            Box(Modifier.height(8.dp))
        }
        Surface(color = scheme.surfaceContainerLow, tonalElevation = 2.dp) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (secondaryLabel != null && onSecondary != null) {
                    OutlinedButton(
                        onClick = onSecondary,
                        enabled = primaryEnabled,
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape = RoundedCornerShape(50),
                    ) { Text(secondaryLabel, style = MaterialTheme.typography.labelLarge) }
                }
                Button(
                    onClick = onPrimary,
                    enabled = primaryEnabled,
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = scheme.primaryContainer, contentColor = scheme.onPrimaryContainer),
                ) {
                    Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text("  $primaryLabel", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

/** A titled group of fields inside one card, like the sections of the reference design. */
@Composable
fun FormSection(title: String, icon: ImageVector? = null, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (icon != null) Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Text(title, style = MaterialTheme.typography.titleMedium)
        }
        RoozYarCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(18.dp), content = content)
        }
    }
}

@Composable
fun FieldLabel(label: String, tag: FieldTag = FieldTag.None) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
        tag.label?.let {
            val color = if (tag == FieldTag.Required) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            Surface(shape = RoundedCornerShape(50), color = color.copy(alpha = 0.15f)) {
                Text(it, modifier = Modifier.padding(horizontal = 8.dp, vertical = 1.dp), style = MaterialTheme.typography.labelSmall, color = color)
            }
        }
    }
}

@Composable
private fun fieldColors() = TextFieldDefaults.colors(
    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    errorContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    focusedIndicatorColor = Color.Transparent,
    unfocusedIndicatorColor = Color.Transparent,
    errorIndicatorColor = Color.Transparent,
    disabledIndicatorColor = Color.Transparent,
)

/** Rounded text field with its label above, an optional requirement tag, helper or error text and a character counter. */
@Composable
fun FormField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    placeholder: String? = null,
    tag: FieldTag = FieldTag.None,
    helper: String? = null,
    error: String? = null,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxChars: Int? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
) {
    val scheme = MaterialTheme.colorScheme
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        FieldLabel(label, tag)
        TextField(
            value = value,
            onValueChange = { if (maxChars == null || it.length <= maxChars) onValueChange(it) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = placeholder?.let { { Text(it, color = scheme.onSurfaceVariant.copy(alpha = 0.7f)) } },
            leadingIcon = leadingIcon?.let { { Icon(it, contentDescription = null, tint = scheme.onSurfaceVariant) } },
            singleLine = singleLine,
            minLines = minLines,
            isError = error != null,
            shape = RoundedCornerShape(14.dp),
            colors = fieldColors(),
            keyboardOptions = keyboardOptions,
        )
        Row(Modifier.fillMaxWidth()) {
            val note = error ?: helper
            if (note != null) {
                Text(
                    note,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (error != null) scheme.error else scheme.onSurfaceVariant,
                )
            } else {
                Box(Modifier.weight(1f))
            }
            if (maxChars != null) {
                Text(
                    "${value.length.toPersianDigits()}/${maxChars.toPersianDigits()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.onSurfaceVariant,
                    textAlign = TextAlign.End,
                )
            }
        }
    }
}

/** A tappable field for values chosen in a dialog (dates, times). Shows [placeholder] until there is a value. */
@Composable
fun PickerField(
    label: String,
    valueText: String?,
    placeholder: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tag: FieldTag = FieldTag.None,
    onClear: (() -> Unit)? = null,
    helper: String? = null,
    error: String? = null,
) {
    val scheme = MaterialTheme.colorScheme
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        FieldLabel(label, tag)
        Surface(onClick = onClick, shape = RoundedCornerShape(14.dp), color = scheme.surfaceContainerHigh) {
            Row(Modifier.fillMaxWidth().padding(start = 14.dp, top = 6.dp, bottom = 6.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = if (valueText != null) scheme.primary else scheme.onSurfaceVariant)
                Text(
                    valueText ?: placeholder,
                    modifier = Modifier.weight(1f).padding(horizontal = 12.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (valueText != null) scheme.onSurface else scheme.onSurfaceVariant.copy(alpha = 0.7f),
                )
                if (onClear != null && valueText != null) {
                    IconButton(onClick = onClear) { Icon(Icons.Filled.Close, contentDescription = "پاک‌کردن", tint = scheme.onSurfaceVariant) }
                }
            }
        }
        val note = error ?: helper
        if (note != null) Text(note, style = MaterialTheme.typography.bodySmall, color = if (error != null) scheme.error else scheme.onSurfaceVariant)
    }
}

/** One row of pill chips to pick a single option; [T] may be nullable to offer a "none" choice. */
@Composable
fun <T> ChoiceChips(
    options: List<T>,
    selected: T,
    onSelect: (T) -> Unit,
    labelOf: (T) -> String,
    modifier: Modifier = Modifier,
    label: String? = null,
    tag: FieldTag = FieldTag.None,
    helper: String? = null,
) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (label != null) FieldLabel(label, tag)
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { o ->
                FilterChip(selected = o == selected, onClick = { onSelect(o) }, label = { Text(labelOf(o)) }, shape = RoundedCornerShape(50))
            }
        }
        if (helper != null) Text(helper, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/** Pill chips where any number can be selected. */
@Composable
fun <T> MultiChoiceChips(
    options: List<T>,
    selected: Set<T>,
    onToggle: (T) -> Unit,
    labelOf: (T) -> String,
    modifier: Modifier = Modifier,
    label: String? = null,
    tag: FieldTag = FieldTag.None,
    error: String? = null,
) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (label != null) FieldLabel(label, tag)
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { o ->
                FilterChip(selected = o in selected, onClick = { onToggle(o) }, label = { Text(labelOf(o)) }, shape = RoundedCornerShape(50))
            }
        }
        if (error != null) Text(error, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
    }
}

@Composable
fun SwitchRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit, modifier: Modifier = Modifier, description: String? = null) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(label, style = MaterialTheme.typography.bodyLarge)
            if (description != null) Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

/** A − value + control for small whole numbers. */
@Composable
fun StepperRow(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    range: IntRange,
    modifier: Modifier = Modifier,
    suffix: String? = null,
) {
    val scheme = MaterialTheme.colorScheme
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        FieldLabel(label)
        Surface(shape = RoundedCornerShape(14.dp), color = scheme.surfaceContainerHigh) {
            Row(Modifier.fillMaxWidth().padding(4.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { onValueChange(value - 1) }, enabled = value > range.first) { Icon(Icons.Filled.Remove, contentDescription = "کم کردن") }
                Text(
                    value.toPersianDigits() + (suffix?.let { " $it" } ?: ""),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium,
                )
                IconButton(onClick = { onValueChange(value + 1) }, enabled = value < range.last) { Icon(Icons.Filled.Add, contentDescription = "افزودن") }
            }
        }
    }
}
