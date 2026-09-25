package com.example.focusgrid.data.repository

import android.content.Context
import androidx.room.withTransaction
import com.example.focusgrid.data.database.DailyFocusDao
import com.example.focusgrid.data.database.DailyFocusEntity
import com.example.focusgrid.data.database.FocusGridDatabase
import com.example.focusgrid.data.database.FocusSessionDao
import com.example.focusgrid.data.database.FocusSessionEntity
import com.example.focusgrid.data.datastore.ActiveSessionPreferences
import com.example.focusgrid.data.datastore.ActiveSessionStore
import com.example.focusgrid.data.datastore.ThemeSettingsStore
import com.example.focusgrid.domain.theme.ThemeColorScheme
import com.example.focusgrid.domain.timer.SessionSplitter
import com.example.focusgrid.widget.WidgetUpdateManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

class FocusRepository(
    private val sessionDao: FocusSessionDao,
    private val dailyDao: DailyFocusDao,
    private val activeSessionStore: ActiveSessionStore,
    private val themeSettingsStore: ThemeSettingsStore,
    private val context: Context
) {
    private val mutex = Mutex()

    val activeSessionFlow: Flow<ActiveSessionPreferences> = activeSessionStore.activeSessionFlow
    val allDailyFocusFlow: Flow<List<DailyFocusEntity>> = dailyDao.getAllDailyFocus()
    val allSessionsFlow: Flow<List<FocusSessionEntity>> = sessionDao.getAllSessions()
    val themeColorSchemeFlow: Flow<ThemeColorScheme> = themeSettingsStore.themeFlow

    suspend fun setThemeColorScheme(scheme: ThemeColorScheme) {
        themeSettingsStore.saveTheme(scheme)
        WidgetUpdateManager.updateWidgets(context)
    }

    fun getSessionsForDate(date: String): Flow<List<FocusSessionEntity>> =
        sessionDao.getSessionsForDate(date)

    fun getDailyFocusBetween(startDate: String, endDate: String): Flow<List<DailyFocusEntity>> =
        dailyDao.getDailyFocusBetween(startDate, endDate)

    suspend fun startSession(): ActiveSessionPreferences = mutex.withLock {
        val now = System.currentTimeMillis()
        val zoneId = ZoneId.systemDefault().id
        val newSession = ActiveSessionPreferences(
            status = "RUNNING",
            sessionId = now,
            startedAtEpochMillis = now,
            runningSinceEpochMillis = now,
            accumulatedMillis = 0L,
            pauseStartedAtEpochMillis = 0L,
            timezoneIdAtStart = zoneId
        )
        activeSessionStore.saveSessionState(newSession)
        WidgetUpdateManager.updateWidgets(context)
        return newSession
    }

    suspend fun pauseSession(): ActiveSessionPreferences = mutex.withLock {
        val current = activeSessionStore.activeSessionFlow.first()
        if (current.status != "RUNNING") return current

        val now = System.currentTimeMillis()
        val addedMillis = maxOf(0L, now - current.runningSinceEpochMillis)
        val updated = current.copy(
            status = "PAUSED",
            accumulatedMillis = current.accumulatedMillis + addedMillis,
            pauseStartedAtEpochMillis = now
        )
        activeSessionStore.saveSessionState(updated)
        WidgetUpdateManager.updateWidgets(context)
        return updated
    }

    suspend fun resumeSession(): ActiveSessionPreferences = mutex.withLock {
        val current = activeSessionStore.activeSessionFlow.first()
        if (current.status != "PAUSED") return current

        val now = System.currentTimeMillis()
        val updated = current.copy(
            status = "RUNNING",
            runningSinceEpochMillis = now,
            pauseStartedAtEpochMillis = 0L
        )
        activeSessionStore.saveSessionState(updated)
        WidgetUpdateManager.updateWidgets(context)
        return updated
    }

    suspend fun finishSession(): Boolean = mutex.withLock {
        val current = activeSessionStore.activeSessionFlow.first()
        if (current.status != "RUNNING" && current.status != "PAUSED") {
            return false
        }

        val now = System.currentTimeMillis()
        val elapsed = current.calculateElapsedMillis(now)

        // Lock state to FINISHING to prevent duplicate saves
        activeSessionStore.saveSessionState(current.copy(status = "FINISHING"))

        if (elapsed > 0) {
            val startedAt = current.startedAtEpochMillis
            val zoneId = ZoneId.of(current.timezoneIdAtStart)

            val startZdt = ZonedDateTime.ofInstant(Instant.ofEpochMilli(startedAt), zoneId)
            val endZdt = ZonedDateTime.ofInstant(Instant.ofEpochMilli(now), zoneId)

            val startDateStr = startZdt.toLocalDate().toString()
            val endDateStr = endZdt.toLocalDate().toString()

            val sessionEntity = FocusSessionEntity(
                startedAtEpochMillis = startedAt,
                endedAtEpochMillis = now,
                durationMillis = elapsed,
                localStartDate = startDateStr,
                localEndDate = endDateStr,
                wasManuallyFinished = true
            )

            // Split session across midnights and update database atomically
            val db = FocusGridDatabase.getDatabase(context)
            db.withTransaction {
                sessionDao.insertSession(sessionEntity)

                val chunks = SessionSplitter.splitSessionAcrossDates(startedAt, now, zoneId)
                for (chunk in chunks) {
                    val existing = dailyDao.getDailyFocus(chunk.dateString)
                    val currentMillis = existing?.focusedMillis ?: 0L
                    val currentCount = existing?.sessionCount ?: 0

                    val updatedDaily = DailyFocusEntity(
                        date = chunk.dateString,
                        focusedMillis = currentMillis + chunk.durationMillis,
                        sessionCount = if (chunk.isStartDay) currentCount + 1 else currentCount
                    )
                    dailyDao.insertOrUpdateDailyFocus(updatedDaily)
                }
            }
        }

        activeSessionStore.clearSession()
        WidgetUpdateManager.updateWidgets(context)
        return true
    }

    suspend fun discardSession(): Unit = mutex.withLock {
        activeSessionStore.clearSession()
        WidgetUpdateManager.updateWidgets(context)
    }
}
