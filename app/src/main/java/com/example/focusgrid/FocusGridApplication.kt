package com.example.focusgrid

import android.app.Application
import com.example.focusgrid.data.database.FocusGridDatabase
import com.example.focusgrid.data.datastore.ActiveSessionStore
import com.example.focusgrid.data.datastore.ThemeSettingsStore
import com.example.focusgrid.data.repository.FocusRepository
import com.example.focusgrid.domain.timer.FocusTimerEngine

class FocusGridApplication : Application() {

    lateinit var database: FocusGridDatabase
        private set

    lateinit var activeSessionStore: ActiveSessionStore
        private set

    lateinit var themeSettingsStore: ThemeSettingsStore
        private set

    lateinit var repository: FocusRepository
        private set

    lateinit var timerEngine: FocusTimerEngine
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        database = FocusGridDatabase.getDatabase(this)
        activeSessionStore = ActiveSessionStore(this)
        themeSettingsStore = ThemeSettingsStore(this)
        repository = FocusRepository(
            sessionDao = database.focusSessionDao(),
            dailyDao = database.dailyFocusDao(),
            activeSessionStore = activeSessionStore,
            themeSettingsStore = themeSettingsStore,
            context = this
        )
        timerEngine = FocusTimerEngine(repository)
        com.example.focusgrid.widget.WidgetUpdateManager.initialize(this, repository)
    }

    companion object {
        lateinit var instance: FocusGridApplication
            private set
    }
}
