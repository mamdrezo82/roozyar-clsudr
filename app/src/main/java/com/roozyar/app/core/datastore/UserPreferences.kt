package com.roozyar.app.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.roozYarDataStore: DataStore<Preferences> by preferencesDataStore(name = "roozyar_preferences")

/** App-level settings (not user content). Content lives in Room. */
class UserPreferences(private val context: Context) {
    val usePersianDigits: Flow<Boolean> = context.roozYarDataStore.data.map { it[Keys.UsePersianDigits] ?: true }

    suspend fun setUsePersianDigits(enabled: Boolean) {
        context.roozYarDataStore.edit { it[Keys.UsePersianDigits] = enabled }
    }

    private object Keys {
        val UsePersianDigits = booleanPreferencesKey("use_persian_digits")
    }
}
