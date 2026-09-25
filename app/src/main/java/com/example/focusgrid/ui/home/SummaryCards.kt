package com.example.focusgrid.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTimeFilled
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.focusgrid.domain.statistics.FocusStatistics
import com.example.focusgrid.domain.theme.ThemeColorScheme
import com.example.focusgrid.ui.theme.CardSurface
import com.example.focusgrid.ui.theme.PrimaryText
import com.example.focusgrid.ui.theme.SecondaryText

@Composable
fun SummaryCards(
    statistics: FocusStatistics,
    colorScheme: ThemeColorScheme = ThemeColorScheme.RED,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.testTag("summary_cards_row"),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        item {
            SummaryCard(
                label = "TODAY",
                value = formatDurationHoursMins(statistics.todayMillis),
                sublabel = "Focus logged",
                icon = Icons.Default.AccessTimeFilled,
                iconTint = colorScheme.veryHighColor,
                testTag = "summary_card_today"
            )
        }
        item {
            SummaryCard(
                label = "STREAK",
                value = "${statistics.streakDays} ${if (statistics.streakDays == 1) "day" else "days"}",
                sublabel = if (statistics.streakDays > 0) "Daily consistency" else "Start today",
                icon = Icons.Default.LocalFireDepartment,
                iconTint = if (statistics.streakDays > 0) Color(0xFFFF9800) else SecondaryText,
                testTag = "summary_card_streak"
            )
        }
        item {
            SummaryCard(
                label = "THIS WEEK",
                value = formatDurationHoursMins(statistics.thisWeekMillis),
                sublabel = "Rolling 7 days",
                icon = Icons.Default.TrendingUp,
                iconTint = colorScheme.highColor,
                testTag = "summary_card_this_week"
            )
        }
        item {
            SummaryCard(
                label = "SESSIONS",
                value = "${statistics.thisWeekSessionCount}",
                sublabel = "Cycles completed",
                icon = Icons.Default.CheckCircle,
                iconTint = colorScheme.veryHighColor,
                testTag = "summary_card_sessions"
            )
        }
    }
}

@Composable
private fun SummaryCard(
    label: String,
    value: String,
    sublabel: String,
    icon: ImageVector,
    iconTint: Color,
    testTag: String
) {
    Card(
        modifier = Modifier
            .width(136.dp)
            .height(112.dp)
            .testTag(testTag),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(1.dp, Color(0xFF262626), RoundedCornerShape(14.dp))
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top row with label and mini icon pill
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = SecondaryText,
                        letterSpacing = 0.6.sp
                    )

                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(iconTint.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = iconTint,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }

                // Middle & bottom value + subtitle
                Column {
                    Text(
                        text = value,
                        fontSize = 19.sp,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryText,
                        letterSpacing = (-0.3).sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = sublabel,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.SansSerif,
                        color = SecondaryText
                    )
                }
            }
        }
    }
}

private fun formatDurationHoursMins(durationMillis: Long): String {
    val totalMinutes = durationMillis / (60 * 1000L)
    val hours = totalMinutes / 60
    val mins = totalMinutes % 60

    return when {
        hours > 0 -> "${hours}h ${mins}m"
        mins > 0 -> "${mins}m"
        else -> "0m"
    }
}
