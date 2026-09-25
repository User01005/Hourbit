package com.example.focusgrid.domain.timer

import com.example.focusgrid.data.repository.FocusRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow

class FocusTimerEngine(private val repository: FocusRepository) {

    companion object {
        const val CYCLE_DURATION_MILLIS = 25 * 60 * 1000L // 25 minutes
    }

    val timerStateFlow: Flow<TimerDisplayState> = flow {
        while (true) {
            val now = System.currentTimeMillis()
            val active = repository.activeSessionFlow.firstOrNull()

            if (active == null || !active.isSessionActive) {
                emit(TimerDisplayState.IDLE)
            } else {
                val status = when (active.status) {
                    "RUNNING" -> SessionStatus.RUNNING
                    "PAUSED" -> SessionStatus.PAUSED
                    "FINISHING" -> SessionStatus.FINISHING
                    else -> SessionStatus.IDLE
                }

                val elapsed = active.calculateElapsedMillis(now)
                val cycleNumber = (elapsed / CYCLE_DURATION_MILLIS).toInt()
                val progressInCycle = ((elapsed % CYCLE_DURATION_MILLIS).toFloat() / CYCLE_DURATION_MILLIS.toFloat()).coerceIn(0f, 1f)
                val isFlipped = (cycleNumber % 2 != 0)

                emit(
                    TimerDisplayState(
                        status = status,
                        elapsedMillis = elapsed,
                        cycleNumber = cycleNumber,
                        cycleProgress = progressInCycle,
                        orientationIsInverted = isFlipped,
                        formattedTime = TimerDisplayState.formatDuration(elapsed)
                    )
                )
            }

            delay(200) // Ticker refresh rate for smooth display
        }
    }
}
