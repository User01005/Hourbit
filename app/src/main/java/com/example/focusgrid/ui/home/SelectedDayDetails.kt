package com.example.focusgrid.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.focusgrid.data.database.FocusSessionEntity
import com.example.focusgrid.domain.theme.ThemeColorScheme
import com.example.focusgrid.ui.theme.CardSurface
import com.example.focusgrid.ui.theme.PrimaryText
import com.example.focusgrid.ui.theme.SecondaryText
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun SelectedDayDetails(
    selectedDate: LocalDate,
    sessions: List<FocusSessionEntity>,
    colorScheme: ThemeColorScheme = ThemeColorScheme.RED,
    modifier: Modifier = Modifier
) {
    val isToday = (selectedDate == LocalDate.now())
    val isYesterday = (selectedDate == LocalDate.now().minusDays(1))

    val dateHeader = when {
        isToday -> "TODAY · ${selectedDate.format(DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.US))}"
        isYesterday -> "YESTERDAY · ${selectedDate.format(DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.US))}"
        else -> selectedDate.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy", Locale.US))
    }

    val totalDurationMillis = sessions.sumOf { it.durationMillis }
    val longestSessionMillis = sessions.maxOfOrNull { it.durationMillis } ?: 0L

    // Benchmark comparison: 2 hours (120 mins) daily benchmark
    val targetDailyMillis = 120 * 60 * 1000L
    val dailyProgress = (totalDurationMillis.toFloat() / targetDailyMillis.toFloat()).coerceIn(0f, 1f)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag("selected_day_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFF262626), RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            // Header row with calendar badge
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
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "Day Details",
                            tint = colorScheme.veryHighColor,
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "DAY SUMMARY",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = SecondaryText,
                            letterSpacing = 0.6.sp
                        )
                        Text(
                            text = dateHeader,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.SemiBold,
                            color = PrimaryText
                        )
                    }
                }

                if (isToday) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(colorScheme.veryHighColor.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "CURRENT",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.veryHighColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (sessions.isEmpty()) {
                // Friendly Empty State
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1E1E1E))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF282828)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SelfImprovement,
                                contentDescription = null,
                                tint = SecondaryText,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "No focus logged for this date",
                                fontSize = 13.sp,
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.Medium,
                                color = PrimaryText
                            )
                            Text(
                                text = if (isToday) "Start a session above to light up today's cell!" else "Select another day on the grid to inspect details",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.SansSerif,
                                color = SecondaryText
                            )
                        }
                    }
                }
            } else {
                // Key Metrics Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1E1E1E))
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MetricItem(
                        label = "TOTAL TIME",
                        value = formatDuration(totalDurationMillis),
                        accentColor = colorScheme.veryHighColor
                    )

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(32.dp)
                            .background(Color(0xFF2E2E2E))
                    )

                    MetricItem(
                        label = "SESSIONS",
                        value = "${sessions.size}",
                        accentColor = PrimaryText
                    )

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(32.dp)
                            .background(Color(0xFF2E2E2E))
                    )

                    MetricItem(
                        label = "LONGEST",
                        value = formatDuration(longestSessionMillis),
                        accentColor = PrimaryText
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Progress towards daily 2h target
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Daily Benchmark (2h target)",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.SansSerif,
                            color = SecondaryText
                        )
                        Text(
                            text = "${(dailyProgress * 100).toInt()}%",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.veryHighColor
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { dailyProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = colorScheme.veryHighColor,
                        trackColor = Color(0xFF282828)
                    )
                }

                // Mini session breakdown pills
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "RECORDED CYCLES",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = SecondaryText,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.US)
                    val zoneId = ZoneId.systemDefault()

                    sessions.sortedByDescending { it.startedAtEpochMillis }.take(4).forEach { session ->
                        val start = Instant.ofEpochMilli(session.startedAtEpochMillis).atZone(zoneId).format(timeFormatter)
                        val end = Instant.ofEpochMilli(session.endedAtEpochMillis).atZone(zoneId).format(timeFormatter)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF232323))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(colorScheme.veryHighColor)
                                )
                                Text(
                                    text = "$start – $end",
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = PrimaryText
                                )
                            }
                            Text(
                                text = formatDuration(session.durationMillis),
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.veryHighColor
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricItem(
    label: String,
    value: String,
    accentColor: Color
) {
    Column {
        Text(
            text = label,
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = SecondaryText,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 17.sp,
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Bold,
            color = accentColor
        )
    }
}

private fun formatDuration(durationMillis: Long): String {
    val totalMinutes = durationMillis / (60 * 1000L)
    val hours = totalMinutes / 60
    val mins = totalMinutes % 60

    return when {
        hours > 0 -> "${hours}h ${mins}m"
        mins > 0 -> "${mins}m"
        else -> "<1m"
    }
}
