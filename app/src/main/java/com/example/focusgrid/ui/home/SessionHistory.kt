package com.example.focusgrid.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.focusgrid.ui.theme.DividerLines
import com.example.focusgrid.ui.theme.PrimaryText
import com.example.focusgrid.ui.theme.SecondaryText
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun SessionHistory(
    allSessions: List<FocusSessionEntity>,
    colorScheme: ThemeColorScheme = ThemeColorScheme.RED,
    modifier: Modifier = Modifier
) {
    var isSectionExpanded by remember { mutableStateOf(true) }

    val groupedSessions = allSessions
        .groupBy { it.localStartDate }
        .entries
        .sortedByDescending { it.key }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag("session_history_section")
    ) {
        // Collapsible Header Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .clickable { isSectionExpanded = !isSectionExpanded }
                .padding(vertical = 6.dp, horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(colorScheme.veryHighColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = colorScheme.veryHighColor,
                        modifier = Modifier.size(14.dp)
                    )
                }

                Text(
                    text = "SESSION HISTORY",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = SecondaryText,
                    letterSpacing = 0.6.sp
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF262626))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${allSessions.size}",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryText
                    )
                }
            }

            Icon(
                imageVector = if (isSectionExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = if (isSectionExpanded) "Collapse History" else "Expand History",
                tint = SecondaryText,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        AnimatedVisibility(
            visible = isSectionExpanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            if (groupedSessions.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardSurface)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFF262626), RoundedCornerShape(16.dp))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF222222)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.HourglassBottom,
                                    contentDescription = null,
                                    tint = colorScheme.veryHighColor,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Text(
                                text = "No focus history yet",
                                fontSize = 14.sp,
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.SemiBold,
                                color = PrimaryText
                            )
                            Text(
                                text = "Start your first focus cycle above to track your journey.",
                                fontSize = 12.sp,
                                fontFamily = FontFamily.SansSerif,
                                color = SecondaryText
                            )
                        }
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    groupedSessions.forEach { (dateStr, sessions) ->
                        DateHistoryGroupCard(
                            dateStr = dateStr,
                            sessions = sessions,
                            colorScheme = colorScheme
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DateHistoryGroupCard(
    dateStr: String,
    sessions: List<FocusSessionEntity>,
    colorScheme: ThemeColorScheme
) {
    var isGroupExpanded by remember { mutableStateOf(true) }

    val date = try {
        LocalDate.parse(dateStr)
    } catch (_: Exception) {
        LocalDate.now()
    }

    val today = LocalDate.now()
    val isToday = (date == today)
    val isYesterday = (date == today.minusDays(1))

    val dateLabel = when {
        isToday -> "TODAY · ${date.format(DateTimeFormatter.ofPattern("MMM d", Locale.US)).uppercase()}"
        isYesterday -> "YESTERDAY · ${date.format(DateTimeFormatter.ofPattern("MMM d", Locale.US)).uppercase()}"
        else -> date.format(DateTimeFormatter.ofPattern("EEE · MMM d", Locale.US)).uppercase()
    }

    val totalDuration = sessions.sumOf { it.durationMillis }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("history_group_$dateStr"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFF262626), RoundedCornerShape(14.dp))
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isGroupExpanded = !isGroupExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = if (isGroupExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Toggle Group",
                        tint = SecondaryText,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = dateLabel,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = SecondaryText
                    )
                    Text(
                        text = "(${sessions.size})",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = SecondaryText
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(colorScheme.veryHighColor.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = formatDuration(totalDuration),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.veryHighColor
                    )
                }
            }

            AnimatedVisibility(
                visible = isGroupExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(10.dp))

                    val sortedSessions = sessions.sortedByDescending { it.startedAtEpochMillis }
                    sortedSessions.forEachIndexed { index, session ->
                        if (index > 0) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 6.dp),
                                color = DividerLines
                            )
                        }
                        SessionItemRow(
                            session = session,
                            colorScheme = colorScheme
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SessionItemRow(
    session: FocusSessionEntity,
    colorScheme: ThemeColorScheme
) {
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.US)
    val zoneId = ZoneId.systemDefault()

    val startTime = Instant.ofEpochMilli(session.startedAtEpochMillis)
        .atZone(zoneId)
        .format(timeFormatter)

    val endTime = Instant.ofEpochMilli(session.endedAtEpochMillis)
        .atZone(zoneId)
        .format(timeFormatter)

    val durationText = formatDuration(session.durationMillis)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(colorScheme.veryHighColor)
            )

            Text(
                text = "$startTime – $endTime",
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium,
                color = PrimaryText
            )
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF222222))
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Text(
                text = durationText,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold,
                color = SecondaryText
            )
        }
    }
}

private fun formatDuration(durationMillis: Long): String {
    val totalMinutes = durationMillis / (60 * 1000L)
    val hours = totalMinutes / 60
    val mins = totalMinutes % 60

    return when {
        hours > 0 -> "${hours}h ${mins}m"
        mins > 0 -> "${mins} min"
        else -> "<1 min"
    }
}
