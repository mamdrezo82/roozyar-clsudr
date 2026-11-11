package com.roozyar.core.datastore

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.roozYarDataStore by preferencesDataStore(name = "roozyar_preferences")

class UserPreferences(private val context: Context) {
    val usePersianDigits: Flow<Boolean> = context.roozYarDataStore.data
        .map { it[Keys.UsePersianDigits] ?: true }

    suspend fun setUsePersianDigits(enabled: Boolean) {
        context.roozYarDataStore.edit { it[Keys.UsePersianDigits] = enabled }
    }

    private object Keys {
        val UsePersianDigits: Preferences.Key<Boolean> = booleanPreferencesKey("use_persian_digits")
    }
}
