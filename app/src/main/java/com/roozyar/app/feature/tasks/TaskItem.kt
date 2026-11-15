package com.roozyar.app.feature.tasks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.roozyar.app.core.design.RoozYarCard
import com.roozyar.app.domain.model.Task
import com.roozyar.app.domain.model.TaskPriority
import com.roozyar.app.domain.model.TaskStatus
import com.roozyar.app.domain.model.isOverdue
import java.time.Instant
import java.time.ZoneId

/** One task row, shared by the Tasks list and the Dashboard. Pass [onDelete] = null to hide the delete button. */
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
    RoozYarCard(modifier = modifier.fillMaxWidth(), onClick = onClick) {
        Row(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onToggle) {
                Icon(
                    imageVector = if (done) Icons.Filled.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                    contentDescription = if (done) "بازگردانی وظیفه" else "انجام شد",
                    tint = if (done) scheme.primary else scheme.onSurfaceVariant,
                )
            }
            Column(Modifier.weight(1f).padding(vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    task.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = if (done) scheme.onSurfaceVariant else scheme.onSurface,
                    textDecoration = if (done) TextDecoration.LineThrough else null,
                )
                val due = formatDue(task, zone)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    PriorityBadge(task.priority)
                    if (due != null) {
                        Text(
                            if (overdue) "$due • عقب‌افتاده" else due,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (overdue) scheme.error else scheme.onSurfaceVariant,
                        )
                    }
                }
            }
            if (onDelete != null) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Outlined.Delete, contentDescription = "حذف", tint = scheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun PriorityBadge(priority: TaskPriority) {
    val scheme = MaterialTheme.colorScheme
    val color = when (priority) {
        TaskPriority.CRITICAL -> scheme.error
        TaskPriority.IMPORTANT -> scheme.secondary
        TaskPriority.NORMAL -> scheme.onSurfaceVariant
    }
    Surface(shape = MaterialTheme.shapes.small, color = color.copy(alpha = 0.15f)) {
        Text(
            priorityLabel(priority),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
        )
    }
}
