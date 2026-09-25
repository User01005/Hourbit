package com.example.focusgrid.ui.hourglass

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.focusgrid.domain.theme.ThemeColorScheme

private val FocusBackground = Color(0xFF141414)
private val PrimaryText = Color(0xFFF4F4F4)
private val SecondaryText = Color(0xFF8A8A8A)
private const val CYCLE_DURATION_MS = 25L * 60L * 1_000L

@Composable
fun FocusHourglassSection(
    currentElapsedMillis: () -> Long,
    totalTodayMillis: Long,
    isRunning: Boolean,
    onSingleTap: () -> Unit = {},
    onDoubleTap: () -> Unit = {},
    colorScheme: ThemeColorScheme = ThemeColorScheme.RED,
    onFinishSession: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val elapsed = currentElapsedMillis().coerceAtLeast(0L)
    val cycleIndex = (elapsed / CYCLE_DURATION_MS) + 1
    val cycleProgress = ((elapsed % CYCLE_DURATION_MS).toFloat() / CYCLE_DURATION_MS.toFloat()).coerceIn(0f, 1f)

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(FocusBackground),
        contentAlignment = Alignment.Center
    ) {
        // Ambient radial glow matching the active theme sand color
        Box(
            modifier = Modifier
                .size(340.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            colorScheme.veryHighColor.copy(alpha = if (isRunning) 0.14f else 0.06f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Status Chip
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF202020))
                    .border(1.dp, Color(0xFF303030), RoundedCornerShape(20.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(
                                if (isRunning) colorScheme.veryHighColor.copy(alpha = pulseAlpha)
                                else SecondaryText
                            )
                    )
                    Text(
                        text = if (isRunning) "CYCLE $cycleIndex IN PROGRESS" else "SESSION PAUSED",
                        color = if (isRunning) PrimaryText else SecondaryText,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Smooth Pixel Hourglass Canvas
            Box(
                modifier = Modifier
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { onSingleTap() },
                            onDoubleTap = { onDoubleTap() }
                        )
                    }
                    .testTag("hourglass_canvas_container"),
                contentAlignment = Alignment.Center
            ) {
                SmoothPixelHourglass(
                    currentElapsedMillis = currentElapsedMillis,
                    isRunning = isRunning,
                    sandColor = colorScheme.veryHighColor,
                    modifier = Modifier.size(
                        width = 150.dp,
                        height = 190.dp
                    )
                )
            }

            Spacer(modifier = Modifier.height(26.dp))

            // Timer display
            Text(
                text = formatDuration(elapsed),
                color = PrimaryText,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Light,
                fontSize = 46.sp,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 25-minute cycle progress indicator
            Column(
                modifier = Modifier.width(180.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LinearProgressIndicator(
                    progress = { cycleProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = colorScheme.veryHighColor,
                    trackColor = Color(0xFF262626)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "${(cycleProgress * 100).toInt()}% of 25m cycle",
                    color = SecondaryText,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Interactive Control Dock (Pause/Resume + Finish)
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pause / Resume Toggle Button
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(if (isRunning) Color(0xFF262626) else colorScheme.veryHighColor)
                        .border(1.dp, if (isRunning) Color(0xFF404040) else colorScheme.veryHighColor, CircleShape)
                        .clickable { onSingleTap() }
                        .testTag("timer_play_pause_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isRunning) "Pause Timer" else "Resume Timer",
                        tint = if (isRunning) PrimaryText else Color.Black,
                        modifier = Modifier.size(26.dp)
                    )
                }

                // Finish Session Button
                Box(
                    modifier = Modifier
                        .height(54.dp)
                        .clip(RoundedCornerShape(27.dp))
                        .background(Color(0xFF202020))
                        .border(1.dp, Color(0xFF383838), RoundedCornerShape(27.dp))
                        .clickable {
                            if (onFinishSession != null) {
                                onFinishSession()
                            } else {
                                onDoubleTap()
                            }
                        }
                        .padding(horizontal = 20.dp)
                        .testTag("timer_finish_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Finish Session",
                            tint = colorScheme.veryHighColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "FINISH",
                            color = PrimaryText,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Today Total Footer Chip
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1B1B1B))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "TODAY TOTAL · ${formatHumanTotalDuration(totalTodayMillis)}",
                    color = SecondaryText,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

private fun formatDuration(durationMillis: Long): String {
    val totalSeconds = durationMillis
        .coerceAtLeast(0L)
        .div(1_000L)

    val hours = totalSeconds / 3_600L
    val minutes = (totalSeconds % 3_600L) / 60L
    val seconds = totalSeconds % 60L

    return if (hours > 0L) {
        "%02d:%02d:%02d".format(
            hours,
            minutes,
            seconds
        )
    } else {
        "%02d:%02d".format(
            minutes,
            seconds
        )
    }
}

private fun formatHumanTotalDuration(durationMillis: Long): String {
    val totalMinutes = maxOf(0L, durationMillis) / (60 * 1000L)
    val hours = totalMinutes / 60
    val mins = totalMinutes % 60
    return when {
        hours == 1L -> "1 hr, ${mins} mins"
        hours > 1L -> "${hours} hrs, ${mins} mins"
        else -> "${mins} mins"
    }
}
