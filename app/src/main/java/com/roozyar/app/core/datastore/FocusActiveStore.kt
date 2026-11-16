package com.roozyar.app.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.roozyar.app.domain.model.ActiveFocus
import com.roozyar.app.domain.model.FocusKind
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant

private val Context.focusDataStore: DataStore<Preferences> by preferencesDataStore(name = "focus_active")

/** Persists the currently running focus/break timer (at most one). */
class FocusActiveStore(private val context: Context) {
    val active: Flow<ActiveFocus?> = context.focusDataStore.data.map { p ->
        val kind = p[KIND]?.let { runCatching { FocusKind.valueOf(it) }.getOrNull() }
        val start = p[STARTED]
        val end = p[ENDS]
        val planned = p[PLANNED]
        if (kind == null || start == null || end == null || planned == null) {
            null
        } else {
            ActiveFocus(
                kind = kind,
                startedAt = Instant.ofEpochMilli(start),
                endsAt = Instant.ofEpochMilli(end),
                plannedMinutes = planned,
                taskId = p[TASK_ID]?.takeIf { it > 0 },
                goalId = p[GOAL_ID]?.takeIf { it > 0 },
            )
        }
    }

    suspend fun set(value: ActiveFocus?) {
        context.focusDataStore.edit { p ->
            if (value == null) {
                p.clear()
            } else {
                p[KIND] = value.kind.name
                p[STARTED] = value.startedAt.toEpochMilli()
                p[ENDS] = value.endsAt.toEpochMilli()
                p[PLANNED] = value.plannedMinutes
                p[TASK_ID] = value.taskId ?: 0L
                p[GOAL_ID] = value.goalId ?: 0L
            }
        }
    }

    private companion object {
        val KIND = stringPreferencesKey("kind")
        val STARTED = longPreferencesKey("started")
        val ENDS = longPreferencesKey("ends")
        val PLANNED = intPreferencesKey("planned")
        val TASK_ID = longPreferencesKey("task_id")
        val GOAL_ID = longPreferencesKey("goal_id")
    }
}
