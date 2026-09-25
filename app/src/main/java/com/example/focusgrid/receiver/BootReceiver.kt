package com.example.focusgrid.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.focusgrid.widget.WidgetUpdateManager

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED || action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            val pendingResult = goAsync()
            try {
                WidgetUpdateManager.updateWidgets(context)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
