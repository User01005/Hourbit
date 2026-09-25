package com.example.focusgrid.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.example.focusgrid.FocusGridApplication
import com.example.focusgrid.data.datastore.ActiveSessionPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class FocusWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_WIDGET_TAP = "com.example.focusgrid.ACTION_WIDGET_TAP"

        private var lastTapTime = 0L
        private var pendingSingleTapJob: Job? = null
        private val mainScope = CoroutineScope(Dispatchers.Main)
        private const val DOUBLE_TAP_THRESHOLD_MS = 350L
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_WIDGET_TAP) {
            handleTap(context.applicationContext)
        }
    }

    private fun handleTap(appContext: Context) {
        val now = System.currentTimeMillis()
        if (now - lastTapTime < DOUBLE_TAP_THRESHOLD_MS) {
            // Double tap detected!
            pendingSingleTapJob?.cancel()
            pendingSingleTapJob = null
            lastTapTime = 0L

            mainScope.launch(Dispatchers.IO) {
                val app = appContext as FocusGridApplication
                app.repository.finishSession()
            }
        } else {
            // Potential single tap
            lastTapTime = now
            pendingSingleTapJob?.cancel()
            pendingSingleTapJob = mainScope.launch {
                delay(DOUBLE_TAP_THRESHOLD_MS)
                // Single tap confirmed -> execute toggle action
                mainScope.launch(Dispatchers.IO) {
                    val app = appContext as FocusGridApplication
                    val session = app.repository.activeSessionFlow.firstOrNull() ?: ActiveSessionPreferences(
                        status = "IDLE",
                        sessionId = 0L,
                        startedAtEpochMillis = 0L,
                        runningSinceEpochMillis = 0L,
                        accumulatedMillis = 0L,
                        pauseStartedAtEpochMillis = 0L,
                        timezoneIdAtStart = java.time.ZoneId.systemDefault().id
                    )
                    when (session.status) {
                        "IDLE" -> app.repository.startSession()
                        "RUNNING" -> app.repository.pauseSession()
                        "PAUSED" -> app.repository.resumeSession()
                    }
                }
            }
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                for (id in appWidgetIds) {
                    FocusWidgetRenderer.renderAndApply(context, appWidgetManager, id)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle?
    ) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                FocusWidgetRenderer.renderAndApply(context, appWidgetManager, appWidgetId)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
