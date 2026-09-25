package com.example.focusgrid.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.example.focusgrid.data.repository.FocusRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object WidgetUpdateManager {

    private val scope = CoroutineScope(Dispatchers.IO)
    private var tickingJob: Job? = null

    fun initialize(context: Context, repository: FocusRepository) {
        scope.launch {
            repository.activeSessionFlow.collect { session ->
                updateWidgets(context)
                if (session.status == "RUNNING") {
                    startTicking(context)
                } else {
                    stopTicking()
                }
            }
        }
    }

    private fun startTicking(context: Context) {
        if (tickingJob?.isActive == true) return
        tickingJob = scope.launch {
            while (true) {
                delay(1000)
                updateWidgets(context)
            }
        }
    }

    private fun stopTicking() {
        tickingJob?.cancel()
        tickingJob = null
    }

    fun updateWidgets(context: Context) {
        val intent = Intent(context, FocusWidgetProvider::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            val ids = AppWidgetManager.getInstance(context).getAppWidgetIds(
                ComponentName(context, FocusWidgetProvider::class.java)
            )
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        }
        context.sendBroadcast(intent)
    }
}
