package com.example.focusgrid.domain.calendar

import com.example.focusgrid.domain.heatmap.HeatLevel
import com.example.focusgrid.domain.heatmap.HeatmapClassifier
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

data class YearDayCell(
    val date: LocalDate,
    val columnIndex: Int,
    val rowIndex: Int, // 0 = Monday, 6 = Sunday
    val focusedMillis: Long,
    val heatLevel: HeatLevel,
    val isToday: Boolean
)

data class MonthHeader(
    val label: String,
    val columnIndex: Int
)

data class RollingYearGrid(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val cells: List<YearDayCell>,
    val monthHeaders: List<MonthHeader>,
    val totalColumns: Int
)

object RollingYearCalculator {

    fun calculateGrid(
        today: LocalDate = LocalDate.now(),
        dailyFocusMap: Map<LocalDate, Long> = emptyMap()
    ): RollingYearGrid {
        val currentYear = today.year
        val startDate = LocalDate.of(currentYear, 1, 1)
        val endDate = LocalDate.of(currentYear, 12, 31)

        val cells = ArrayList<YearDayCell>(366)
        val monthHeaders = ArrayList<MonthHeader>()

        var currentColumn = 0
        var lastMonth = -1

        var date = startDate
        while (!date.isAfter(endDate)) {
            val dayOfWeekVal = date.dayOfWeek.value // 1 = Monday, 7 = Sunday
            val rowIndex = dayOfWeekVal - 1

            // Check for new month header
            if (date.monthValue != lastMonth) {
                val monthLabel = date.month.getDisplayName(TextStyle.SHORT, Locale.US).uppercase()
                monthHeaders.add(MonthHeader(monthLabel, currentColumn))
                lastMonth = date.monthValue
            }

            val focusedMillis = dailyFocusMap[date] ?: 0L
            val heatLevel = HeatmapClassifier.classifyMillis(focusedMillis)
            val isToday = (date == today)

            cells.add(
                YearDayCell(
                    date = date,
                    columnIndex = currentColumn,
                    rowIndex = rowIndex,
                    focusedMillis = focusedMillis,
                    heatLevel = heatLevel,
                    isToday = isToday
                )
            )

            if (rowIndex == 6) { // Sunday, move to next week column
                currentColumn++
            }

            date = date.plusDays(1)
        }

        val totalCols = if (cells.isNotEmpty() && cells.last().rowIndex != 6) currentColumn + 1 else currentColumn

        return RollingYearGrid(
            startDate = startDate,
            endDate = endDate,
            cells = cells,
            monthHeaders = monthHeaders,
            totalColumns = totalCols
        )
    }
}
