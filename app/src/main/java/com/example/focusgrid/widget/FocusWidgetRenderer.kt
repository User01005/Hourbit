package com.example.focusgrid.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.focusgrid.FocusGridApplication
import com.example.focusgrid.R
import com.example.focusgrid.data.database.FocusGridDatabase
import com.example.focusgrid.data.datastore.ActiveSessionPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.YearMonth

object FocusWidgetRenderer {

    suspend fun renderAndApply(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) = withContext(Dispatchers.IO) {
        try {
            val app = context.applicationContext as FocusGridApplication
            val session = app.repository.activeSessionFlow.firstOrNull() ?: ActiveSessionPreferences(
                status = "IDLE",
                sessionId = 0L,
                startedAtEpochMillis = 0L,
                runningSinceEpochMillis = 0L,
                accumulatedMillis = 0L,
                pauseStartedAtEpochMillis = 0L,
                timezoneIdAtStart = java.time.ZoneId.systemDefault().id
            )
            val themeScheme = app.repository.themeColorSchemeFlow.firstOrNull() ?: com.example.focusgrid.domain.theme.ThemeColorScheme.RED

            val options = appWidgetManager.getAppWidgetOptions(appWidgetId)
            val minWidthDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 140)
            val minHeightDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 140)

            val density = context.resources.displayMetrics.density
            val widthPx = maxOf(140, (minWidthDp * density).toInt())
            val heightPx = maxOf(140, (minHeightDp * density).toInt())

            val today = LocalDate.now()
            val todayStr = today.toString()
            val db = FocusGridDatabase.getDatabase(context)

            val todayFocusEntity = try {
                db.dailyFocusDao().getDailyFocus(todayStr)
            } catch (_: Exception) {
                null
            }
            val todayTotalMillis = todayFocusEntity?.focusedMillis ?: 0L

            val bitmap = if (session.isSessionActive) {
                val elapsed = session.calculateElapsedMillis(System.currentTimeMillis())
                CalendarBitmapRenderer.renderTimerBitmap(
                    widthPx = widthPx,
                    heightPx = heightPx,
                    status = session.status,
                    elapsedMillis = elapsed,
                    todayTotalMillis = todayTotalMillis,
                    colorScheme = themeScheme
                )
            } else {
                val yearMonth = YearMonth.now()

                val startDate = yearMonth.atDay(1).toString()
                val endDate = yearMonth.atEndOfMonth().toString()

                val dailyMap = try {
                    val dailyList = db.dailyFocusDao().getDailyFocusBetweenList(startDate, endDate)
                    dailyList.associate { LocalDate.parse(it.date) to it.focusedMillis }
                } catch (_: Exception) {
                    emptyMap()
                }

                CalendarBitmapRenderer.renderBitmap(
                    widthPx = widthPx,
                    heightPx = heightPx,
                    yearMonth = yearMonth,
                    dailyFocusMap = dailyMap,
                    today = today,
                    colorScheme = themeScheme
                )
            }

            val views = RemoteViews(context.packageName, R.layout.focus_widget)
            views.setImageViewBitmap(R.id.widget_canvas_image, bitmap)

            val tapIntent = Intent(context, FocusWidgetProvider::class.java).apply {
                action = FocusWidgetProvider.ACTION_WIDGET_TAP
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                appWidgetId,
                tapIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
