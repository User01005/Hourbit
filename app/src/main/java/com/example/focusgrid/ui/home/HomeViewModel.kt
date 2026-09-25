package com.example.focusgrid.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.focusgrid.data.database.DailyFocusEntity
import com.example.focusgrid.data.database.FocusSessionEntity
import com.example.focusgrid.data.datastore.ActiveSessionPreferences
import com.example.focusgrid.data.repository.FocusRepository
import com.example.focusgrid.domain.statistics.FocusStatistics
import com.example.focusgrid.domain.statistics.StatisticsCalculator
import com.example.focusgrid.domain.theme.ThemeColorScheme
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class HomeUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val isTimerActive: Boolean = false,
    val activeSessionStatus: String = "IDLE"
)

class HomeViewModel(private val repository: FocusRepository) : ViewModel() {

    val colorScheme: StateFlow<ThemeColorScheme> = repository.themeColorSchemeFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ThemeColorScheme.RED
        )

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    val activeSession: StateFlow<ActiveSessionPreferences> = repository.activeSessionFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ActiveSessionPreferences("IDLE", 0, 0, 0, 0, 0, "")
        )

    val allDailyFocus: StateFlow<List<DailyFocusEntity>> = repository.allDailyFocusFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val statistics: StateFlow<FocusStatistics> = repository.allDailyFocusFlow.map { list ->
        StatisticsCalculator.calculate(today = LocalDate.now(), dailyFocusList = list)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FocusStatistics(0, 0, 0, 0)
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedDateSessions: StateFlow<List<FocusSessionEntity>> = _selectedDate.flatMapLatest { date ->
        repository.getSessionsForDate(date.toString())
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allSessionsHistory: StateFlow<List<FocusSessionEntity>> = repository.allSessionsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun setColorScheme(scheme: ThemeColorScheme) {
        viewModelScope.launch {
            repository.setThemeColorScheme(scheme)
        }
    }

    fun startNewSession(onStarted: () -> Unit) {
        viewModelScope.launch {
            repository.startSession()
            onStarted()
        }
    }

    class Factory(private val repository: FocusRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(repository) as T
        }
    }
}
