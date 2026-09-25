package com.example.focusgrid.ui.focus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.focusgrid.data.datastore.ActiveSessionPreferences
import com.example.focusgrid.data.repository.FocusRepository
import com.example.focusgrid.domain.theme.ThemeColorScheme
import com.example.focusgrid.domain.timer.FocusTimerEngine
import com.example.focusgrid.domain.timer.SessionStatus
import com.example.focusgrid.domain.timer.TimerDisplayState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class FocusViewModel(
    private val repository: FocusRepository,
    private val timerEngine: FocusTimerEngine
) : ViewModel() {

    val colorScheme: StateFlow<ThemeColorScheme> = repository.themeColorSchemeFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ThemeColorScheme.RED
        )

    val activeSession: StateFlow<ActiveSessionPreferences> = repository.activeSessionFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ActiveSessionPreferences("IDLE", 0L, 0L, 0L, 0L, 0L, "")
        )

    val timerState: StateFlow<TimerDisplayState> = timerEngine.timerStateFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = TimerDisplayState.IDLE
        )

    val todayTotalMillis: StateFlow<Long> = repository.allDailyFocusFlow
        .map { list ->
            val todayStr = LocalDate.now().toString()
            list.find { it.date == todayStr }?.focusedMillis ?: 0L
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0L
        )

    fun togglePauseResume() {
        viewModelScope.launch {
            val currentState = timerState.value
            if (currentState.status == SessionStatus.RUNNING) {
                repository.pauseSession()
            } else if (currentState.status == SessionStatus.PAUSED) {
                repository.resumeSession()
            }
        }
    }

    fun finishSession(onFinished: () -> Unit) {
        viewModelScope.launch {
            repository.finishSession()
            onFinished()
        }
    }

    fun discardSession(onDiscarded: () -> Unit) {
        viewModelScope.launch {
            repository.discardSession()
            onDiscarded()
        }
    }

    class Factory(
        private val repository: FocusRepository,
        private val timerEngine: FocusTimerEngine
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return FocusViewModel(repository, timerEngine) as T
        }
    }
}
