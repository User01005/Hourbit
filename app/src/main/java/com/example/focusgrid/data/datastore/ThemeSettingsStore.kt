package com.example.focusgrid.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.focusgrid.domain.theme.ThemeColorScheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.themeDataStore: DataStore<Preferences> by preferencesDataStore(name = "theme_settings_prefs")

class ThemeSettingsStore(private val context: Context) {

    companion object {
        val COLOR_SCHEME_KEY = stringPreferencesKey("color_scheme_id")
    }

    val themeFlow: Flow<ThemeColorScheme> = context.themeDataStore.data.map { prefs ->
        val id = prefs[COLOR_SCHEME_KEY] ?: ThemeColorScheme.RED.id
        ThemeColorScheme.fromId(id)
    }

    suspend fun saveTheme(scheme: ThemeColorScheme) {
        context.themeDataStore.edit { prefs ->
            prefs[COLOR_SCHEME_KEY] = scheme.id
        }
    }
}
