package com.roozyar.app.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.roozYarDataStore: DataStore<Preferences> by preferencesDataStore(name = "roozyar_preferences")

const val DEFAULT_PLANNER_START = 8 * 60
const val DEFAULT_PLANNER_END = 21 * 60

/** App-level settings (not user content). Content lives in Room. */
class UserPreferences(private val context: Context) {
    val usePersianDigits: Flow<Boolean> = context.roozYarDataStore.data.map { it[Keys.UsePersianDigits] ?: true }

    /** Working window the planner may fill, as minutes after midnight. */
    val plannerStartMinute: Flow<Int> = context.roozYarDataStore.data.map { it[Keys.PlannerStart] ?: DEFAULT_PLANNER_START }
    val plannerEndMinute: Flow<Int> = context.roozYarDataStore.data.map { it[Keys.PlannerEnd] ?: DEFAULT_PLANNER_END }

    suspend fun setPlannerWindow(startMinute: Int, endMinute: Int) {
        context.roozYarDataStore.edit {
            it[Keys.PlannerStart] = startMinute
            it[Keys.PlannerEnd] = endMinute
        }
    }

    suspend fun setUsePersianDigits(enabled: Boolean) {
        context.roozYarDataStore.edit { it[Keys.UsePersianDigits] = enabled }
    }

    private object Keys {
        val UsePersianDigits = booleanPreferencesKey("use_persian_digits")
        val PlannerStart = intPreferencesKey("planner_start_minute")
        val PlannerEnd = intPreferencesKey("planner_end_minute")
    }
}
