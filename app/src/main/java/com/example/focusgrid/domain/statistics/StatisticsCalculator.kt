package com.example.focusgrid.domain.statistics

import com.example.focusgrid.data.database.DailyFocusEntity
import java.time.DayOfWeek
import java.time.LocalDate

data class FocusStatistics(
    val todayMillis: Long,
    val streakDays: Int,
    val thisWeekMillis: Long,
    val thisWeekSessionCount: Int
)

object StatisticsCalculator {

    fun calculate(
        today: LocalDate = LocalDate.now(),
        dailyFocusList: List<DailyFocusEntity>
    ): FocusStatistics {
        val focusMap = dailyFocusList.associateBy { LocalDate.parse(it.date) }

        // 1. Today
        val todayEntity = focusMap[today]
        val todayMillis = todayEntity?.focusedMillis ?: 0L

        // 2. Streak
        // Consecutive calendar days ending today (or yesterday if 0 mins focused today) with at least 1 min (60_000ms)
        var streak = 0
        var checkDate = today
        val todayHasFocus = (focusMap[today]?.focusedMillis ?: 0L) >= 60_000L

        if (!todayHasFocus) {
            checkDate = today.minusDays(1)
        }

        while (true) {
            val entity = focusMap[checkDate]
            val millis = entity?.focusedMillis ?: 0L
            if (millis >= 60_000L) { // At least 1 minute completed
                streak++
                checkDate = checkDate.minusDays(1)
            } else {
                break
            }
        }

        // 3. This Week (Monday through Today)
        val mondayOfThisWeek = today.with(DayOfWeek.MONDAY)
        var thisWeekMillis = 0L
        var thisWeekSessionCount = 0

        var curr = mondayOfThisWeek
        while (!curr.isAfter(today)) {
            val entity = focusMap[curr]
            if (entity != null) {
                thisWeekMillis += entity.focusedMillis
                thisWeekSessionCount += entity.sessionCount
            }
            curr = curr.plusDays(1)
        }

        return FocusStatistics(
            todayMillis = todayMillis,
            streakDays = streak,
            thisWeekMillis = thisWeekMillis,
            thisWeekSessionCount = thisWeekSessionCount
        )
    }
}
