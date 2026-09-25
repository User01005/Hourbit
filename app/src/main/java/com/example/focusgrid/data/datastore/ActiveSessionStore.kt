package com.example.focusgrid.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.ZoneId

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "active_session_prefs")

class ActiveSessionStore(private val context: Context) {

    companion object {
        val STATUS_KEY = stringPreferencesKey("status")
        val SESSION_ID_KEY = longPreferencesKey("session_id")
        val STARTED_AT_KEY = longPreferencesKey("started_at_epoch_millis")
        val RUNNING_SINCE_KEY = longPreferencesKey("running_since_epoch_millis")
        val ACCUMULATED_KEY = longPreferencesKey("accumulated_millis")
        val PAUSE_STARTED_AT_KEY = longPreferencesKey("pause_started_at_epoch_millis")
        val TIMEZONE_KEY = stringPreferencesKey("timezone_id_at_start")
    }

    val activeSessionFlow: Flow<ActiveSessionPreferences> = context.dataStore.data.map { prefs ->
        ActiveSessionPreferences(
            status = prefs[STATUS_KEY] ?: "IDLE",
            sessionId = prefs[SESSION_ID_KEY] ?: 0L,
            startedAtEpochMillis = prefs[STARTED_AT_KEY] ?: 0L,
            runningSinceEpochMillis = prefs[RUNNING_SINCE_KEY] ?: 0L,
            accumulatedMillis = prefs[ACCUMULATED_KEY] ?: 0L,
            pauseStartedAtEpochMillis = prefs[PAUSE_STARTED_AT_KEY] ?: 0L,
            timezoneIdAtStart = prefs[TIMEZONE_KEY] ?: ZoneId.systemDefault().id
        )
    }

    suspend fun saveSessionState(session: ActiveSessionPreferences) {
        context.dataStore.edit { prefs ->
            prefs[STATUS_KEY] = session.status
            prefs[SESSION_ID_KEY] = session.sessionId
            prefs[STARTED_AT_KEY] = session.startedAtEpochMillis
            prefs[RUNNING_SINCE_KEY] = session.runningSinceEpochMillis
            prefs[ACCUMULATED_KEY] = session.accumulatedMillis
            prefs[PAUSE_STARTED_AT_KEY] = session.pauseStartedAtEpochMillis
            prefs[TIMEZONE_KEY] = session.timezoneIdAtStart
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { prefs ->
            prefs[STATUS_KEY] = "IDLE"
            prefs[SESSION_ID_KEY] = 0L
            prefs[STARTED_AT_KEY] = 0L
            prefs[RUNNING_SINCE_KEY] = 0L
            prefs[ACCUMULATED_KEY] = 0L
            prefs[PAUSE_STARTED_AT_KEY] = 0L
            prefs[TIMEZONE_KEY] = ZoneId.systemDefault().id
        }
    }
}
