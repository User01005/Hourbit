package com.example.focusgrid.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.focusgrid.data.database.DailyFocusEntity
import com.example.focusgrid.domain.calendar.RollingYearCalculator
import com.example.focusgrid.domain.calendar.YearDayCell
import com.example.focusgrid.domain.heatmap.HeatLevel
import com.example.focusgrid.domain.theme.ThemeColorScheme
import com.example.focusgrid.ui.theme.CardSurface
import com.example.focusgrid.ui.theme.PrimaryText
import com.example.focusgrid.ui.theme.SecondaryText
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun YearHeatmap(
    dailyFocusList: List<DailyFocusEntity>,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    colorScheme: ThemeColorScheme = ThemeColorScheme.RED,
    modifier: Modifier = Modifier
) {
    val today = LocalDate.now()
    val dailyFocusMap = dailyFocusList.associate { LocalDate.parse(it.date) to it.focusedMillis }
    val gridData = RollingYearCalculator.calculateGrid(today = today, dailyFocusMap = dailyFocusMap)

    val totalRangeMillis = gridData.cells.sumOf { it.focusedMillis }
    val totalMinutes = totalRangeMillis / (60 * 1000L)
    val totalHours = totalMinutes / 60
    val totalMinsRemainder = totalMinutes % 60
    val activeDaysCount = gridData.cells.count { it.focusedMillis > 0L }

    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    // Automatically scroll to the right edge (most recent weeks) on launch
    LaunchedEffect(gridData.totalColumns) {
        scrollState.scrollTo(scrollState.maxValue)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag("year_heatmap_section"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFF262626), RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            // Section Header with branding & jump-to-today action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(colorScheme.veryHighColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "Calendar",
                            tint = colorScheme.veryHighColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "365-DAY HEATMAP",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = SecondaryText,
                            letterSpacing = 0.6.sp
                        )
                        Text(
                            text = "$activeDaysCount active days · ${if (totalHours > 0) "${totalHours}h ${totalMinsRemainder}m" else "${totalMinsRemainder}m"} total",
                            fontSize = 13.sp,
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.SemiBold,
                            color = PrimaryText
                        )
                    }
                }

                // Jump to Today action button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF222222))
                        .border(1.dp, Color(0xFF333333), RoundedCornerShape(8.dp))
                        .clickable {
                            coroutineScope.launch {
                                scrollState.animateScrollTo(scrollState.maxValue)
                            }
                            onDateSelected(today)
                        }
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Today,
                            contentDescription = "Jump to Today",
                            tint = colorScheme.veryHighColor,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "Today",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Heatmap Layout with Weekday Labels on Left & Horizontally Scrollable Grid
            Row(modifier = Modifier.fillMaxWidth()) {
                // Weekday Labels M T W T F S S
                Column(
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                    modifier = Modifier.padding(top = 18.dp, end = 6.dp)
                ) {
                    val weekdays = listOf("M", "T", "W", "T", "F", "S", "S")
                    weekdays.forEach { dayLabel ->
                        Box(
                            modifier = Modifier.size(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = dayLabel,
                                fontSize = 8.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = SecondaryText
                            )
                        }
                    }
                }

                // Horizontally Scrollable Grid
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(scrollState)
                ) {
                    Column {
                        // Month Headers Row
                        Box(modifier = Modifier.height(16.dp)) {
                            gridData.monthHeaders.forEach { header ->
                                val xOffset = (header.columnIndex * 15).dp
                                Text(
                                    text = header.label,
                                    fontSize = 8.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = SecondaryText,
                                    modifier = Modifier.padding(start = xOffset)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        // Grid Columns (Each column is a 7-row week)
                        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            val cellsByCol = gridData.cells.groupBy { it.columnIndex }
                            for (colIndex in 0 until gridData.totalColumns) {
                                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    val colCells = cellsByCol[colIndex]?.associateBy { it.rowIndex } ?: emptyMap()
                                    for (rowIndex in 0..6) {
                                        val cell = colCells[rowIndex]
                                        if (cell != null) {
                                            val isTodayCell = (cell.date == today)
                                            YearHeatmapCell(
                                                cell = cell,
                                                isSelected = (cell.date == selectedDate),
                                                isToday = isTodayCell,
                                                colorScheme = colorScheme,
                                                onSelect = { onDateSelected(cell.date) }
                                            )
                                        } else {
                                            // Empty padding cell for alignment
                                            Box(
                                                modifier = Modifier
                                                    .size(12.dp)
                                                    .background(Color.Transparent)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Heatmap Legend Bar
            HeatmapLegend(colorScheme = colorScheme)
        }
    }
}

@Composable
private fun YearHeatmapCell(
    cell: YearDayCell,
    isSelected: Boolean,
    isToday: Boolean,
    colorScheme: ThemeColorScheme,
    onSelect: () -> Unit
) {
    val cellColor = when (cell.heatLevel) {
        HeatLevel.NONE -> colorScheme.inactiveColor
        HeatLevel.LOW -> colorScheme.lowColor
        HeatLevel.MEDIUM -> colorScheme.mediumColor
        HeatLevel.HIGH -> colorScheme.highColor
        HeatLevel.VERY_HIGH -> colorScheme.veryHighColor
    }

    val focusedMinutes = cell.focusedMillis / (60 * 1000L)
    val formattedDate = cell.date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
    val accessibilityDescription = "$formattedDate, $focusedMinutes focused minutes"

    Box(
        modifier = Modifier
            .size(12.dp)
            .clip(RoundedCornerShape(2.5.dp))
            .background(cellColor)
            .then(
                when {
                    isSelected -> Modifier.border(1.5.dp, colorScheme.outlineColor, RoundedCornerShape(2.5.dp))
                    isToday -> Modifier.border(1.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(2.5.dp))
                    else -> Modifier
                }
            )
            .clickable { onSelect() }
            .semantics { contentDescription = accessibilityDescription }
            .testTag("heatmap_cell_${cell.date}"),
        contentAlignment = Alignment.Center
    ) {
        if (isToday && !isSelected) {
            Box(
                modifier = Modifier
                    .size(3.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            )
        }
    }
}

@Composable
private fun HeatmapLegend(
    colorScheme: ThemeColorScheme,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF1B1B1B))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "LESS",
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = SecondaryText
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LegendSwatch(color = colorScheme.inactiveColor, label = "0m")
            LegendSwatch(color = colorScheme.lowColor, label = "1-50m")
            LegendSwatch(color = colorScheme.mediumColor, label = "51-124m")
            LegendSwatch(color = colorScheme.highColor, label = "125-200m")
            LegendSwatch(color = colorScheme.veryHighColor, label = ">200m")
        }

        Text(
            text = "MORE",
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = SecondaryText
        )
    }
}

@Composable
private fun LegendSwatch(
    color: Color,
    label: String
) {
    Box(
        modifier = Modifier
            .size(10.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(color)
            .semantics { contentDescription = label }
    )
}
