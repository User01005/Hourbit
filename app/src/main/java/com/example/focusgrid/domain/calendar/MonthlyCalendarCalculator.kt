package com.example.focusgrid.domain.heatmap

import com.example.focusgrid.domain.heatmap.HeatLevel
import com.example.focusgrid.domain.heatmap.HeatmapClassifier
import java.time.LocalDate
import java.time.YearMonth

data class MonthlyWidgetCell(
    val gridIndex: Int,
    val date: LocalDate?,
    val dayOfMonth: Int?,
    val isCurrentMonth: Boolean,
    val focusedMillis: Long,
    val heatLevel: HeatLevel,
    val isToday: Boolean
)

object MonthlyCalendarCalculator {

    fun calculateGrid(
        yearMonth: YearMonth,
        dailyFocusMap: Map<LocalDate, Long> = emptyMap(),
        today: LocalDate = LocalDate.now()
    ): List<MonthlyWidgetCell> {
        val firstDay = yearMonth.atDay(1)
        val leadingCells = firstDay.dayOfWeek.value - 1 // Monday=1 -> 0 leading cells
        val lengthOfMonth = yearMonth.lengthOfMonth()

        val cells = ArrayList<MonthlyWidgetCell>(42)

        for (gridIndex in 0 until 42) {
            val dayNumber = gridIndex - leadingCells + 1
            if (dayNumber in 1..lengthOfMonth) {
                val date = yearMonth.atDay(dayNumber)
                val focusedMillis = dailyFocusMap[date] ?: 0L
                val heatLevel = HeatmapClassifier.classifyMillis(focusedMillis)
                val isToday = (date == today)

                cells.add(
                    MonthlyWidgetCell(
                        gridIndex = gridIndex,
                        date = date,
                        dayOfMonth = dayNumber,
                        isCurrentMonth = true,
                        focusedMillis = focusedMillis,
                        heatLevel = heatLevel,
                        isToday = isToday
                    )
                )
            } else {
                cells.add(
                    MonthlyWidgetCell(
                        gridIndex = gridIndex,
                        date = null,
                        dayOfMonth = null,
                        isCurrentMonth = false,
                        focusedMillis = 0L,
                        heatLevel = HeatLevel.NONE,
                        isToday = false
                    )
                )
            }
        }
        return cells
    }
}
