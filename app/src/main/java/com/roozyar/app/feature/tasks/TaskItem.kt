package com.roozyar.app.feature.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.roozyar.app.core.design.RoozYarCard
import com.roozyar.app.domain.model.Task
import com.roozyar.app.domain.model.TaskPriority
import com.roozyar.app.domain.model.TaskStatus
import com.roozyar.app.domain.model.isOverdue
import java.time.Instant
import java.time.ZoneId

@Composable
fun priorityColor(priority: TaskPriority): Color {
    val scheme = MaterialTheme.colorScheme
    return when (priority) {
        TaskPriority.CRITICAL -> scheme.error
        TaskPriority.IMPORTANT -> scheme.secondary
        TaskPriority.NORMAL -> scheme.primary
    }
}

/**
 * One task card, shared by the Tasks list and the Dashboard. A coloured edge on the start side shows the
 * priority. Pass [onDelete] = null to hide the overflow menu.
 */
@Composable
fun TaskItem(
    task: Task,
    now: Instant,
    zone: ZoneId,
    onToggle: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onDelete: (() -> Unit)? = null,
) {
    val scheme = MaterialTheme.colorScheme
    val done = task.status == TaskStatus.DONE
    val overdue = task.isOverdue(now, zone)
    val edge = if (done) scheme.outlineVariant else priorityColor(task.priority)

    RoozYarCard(modifier = modifier.fillMaxWidth(), onClick = onClick) {
        Row(Modifier.height(IntrinsicSize.Min)) {
            Box(Modifier.width(4.dp).fillMaxHeight().background(edge))
            IconButton(onClick = onToggle, modifier = Modifier.align(Alignment.CenterVertically)) {
                Icon(
                    imageVector = if (done) Icons.Filled.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                    contentDescription = if (done) "بازگردانی وظیفه" else "انجام شد",
                    tint = if (done) scheme.primary else scheme.onSurfaceVariant,
                    modifier = Modifier.size(28.dp),
                )
            }
            Column(
                Modifier.weight(1f).padding(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    task.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = if (done) scheme.onSurfaceVariant else scheme.onSurface,
                    textDecoration = if (done) TextDecoration.LineThrough else null,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (task.description.isNotBlank()) {
                    Text(
                        task.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                val due = formatDue(task, zone)
                if (due != null || (task.priority != TaskPriority.NORMAL && !done)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (due != null) {
                            MetaChip(
                                text = due,
                                icon = if (task.dueHasTime) Icons.Outlined.Schedule else Icons.Outlined.CalendarMonth,
                                color = if (overdue) scheme.error else scheme.onSurfaceVariant,
                            )
                        }
                        if (task.priority != TaskPriority.NORMAL && !done) {
                            val c = priorityColor(task.priority)
                            Surface(shape = MaterialTheme.shapes.small, color = c.copy(alpha = 0.15f)) {
                                Text(
                                    priorityLabel(task.priority),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = c,
                                )
                            }
                        }
                    }
                }
            }
            if (onDelete != null) {
                var expanded by remember { mutableStateOf(false) }
                Box(Modifier.align(Alignment.CenterVertically)) {
                    IconButton(onClick = { expanded = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "گزینه‌ها", tint = scheme.onSurfaceVariant)
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        DropdownMenuItem(text = { Text("ویرایش") }, onClick = { expanded = false; onClick() })
                        DropdownMenuItem(
                            text = { Text("حذف", color = scheme.error) },
                            onClick = { expanded = false; onDelete() },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MetaChip(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
        Text(text, style = MaterialTheme.typography.labelMedium, color = color)
    }
}
