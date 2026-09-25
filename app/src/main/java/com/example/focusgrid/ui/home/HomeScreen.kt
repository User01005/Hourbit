package com.example.focusgrid.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.focusgrid.domain.theme.ThemeColorScheme
import com.example.focusgrid.ui.theme.CardSurface
import com.example.focusgrid.ui.theme.MainBackground
import com.example.focusgrid.ui.theme.PrimaryText
import com.example.focusgrid.ui.theme.SecondaryText
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onOpenFocusTimer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val activeSession by viewModel.activeSession.collectAsStateWithLifecycle()
    val allDailyFocus by viewModel.allDailyFocus.collectAsStateWithLifecycle()
    val statistics by viewModel.statistics.collectAsStateWithLifecycle()
    val selectedDateSessions by viewModel.selectedDateSessions.collectAsStateWithLifecycle()
    val allSessionsHistory by viewModel.allSessionsHistory.collectAsStateWithLifecycle()
    val colorScheme by viewModel.colorScheme.collectAsStateWithLifecycle()

    var isThemeSettingsExpanded by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()
    val todayFormatted = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.US))

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MainBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Atmospheric subtle top ambient radial glow matching active theme
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            colorScheme.veryHighColor.copy(alpha = 0.08f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // 1. App Header & Brand Bar
            AppHeaderBar(
                todayFormatted = todayFormatted,
                activeStatus = activeSession.status,
                colorScheme = colorScheme,
                onActiveSessionClick = onOpenFocusTimer
            )

            // 2. Summary Cards with Theme styling
            SummaryCards(
                statistics = statistics,
                colorScheme = colorScheme
            )

            // 3. Flagship Start Focus Session CTA Hero Card
            StartFocusSessionHeroCard(
                activeStatus = activeSession.status,
                colorScheme = colorScheme,
                onCtaClick = {
                    if (activeSession.status == "IDLE") {
                        viewModel.startNewSession(onStarted = onOpenFocusTimer)
                    } else {
                        onOpenFocusTimer()
                    }
                }
            )

            // 4. Color Theme Selector Section
            ThemeSelectorSection(
                currentTheme = colorScheme,
                onSelectTheme = { viewModel.setColorScheme(it) },
                isExpanded = isThemeSettingsExpanded,
                onToggleExpand = { isThemeSettingsExpanded = !isThemeSettingsExpanded }
            )

            // 5. Rolling 365-day Heatmap with Legend
            YearHeatmap(
                dailyFocusList = allDailyFocus,
                selectedDate = selectedDate,
                colorScheme = colorScheme,
                onDateSelected = { viewModel.selectDate(it) }
            )

            // 6. Selected-Day Details
            SelectedDayDetails(
                selectedDate = selectedDate,
                sessions = selectedDateSessions,
                colorScheme = colorScheme
            )

            // 7. Focus-Session History
            SessionHistory(
                allSessions = allSessionsHistory,
                colorScheme = colorScheme
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun AppHeaderBar(
    todayFormatted: String,
    activeStatus: String,
    colorScheme: ThemeColorScheme,
    onActiveSessionClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag("app_header"),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Hourbit micro icon
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(colorScheme.veryHighColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.HourglassTop,
                        contentDescription = "Hourbit Logo",
                        tint = colorScheme.veryHighColor,
                        modifier = Modifier.size(13.dp)
                    )
                }

                Text(
                    text = "HOURBIT",
                    fontSize = 18.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryText,
                    letterSpacing = 2.sp
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = todayFormatted,
                fontSize = 13.sp,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Medium,
                color = SecondaryText
            )
        }

        // Live session chip if active
        if (activeStatus != "IDLE") {
            val infiniteTransition = rememberInfiniteTransition(label = "pulse")
            val pulseAlpha by infiniteTransition.animateFloat(
                initialValue = 0.4f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(800),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "pulseAlpha"
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(colorScheme.veryHighColor.copy(alpha = 0.15f))
                    .border(1.dp, colorScheme.veryHighColor.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                    .clickable { onActiveSessionClick() }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(colorScheme.veryHighColor.copy(alpha = if (activeStatus == "RUNNING") pulseAlpha else 1f))
                    )
                    Text(
                        text = if (activeStatus == "RUNNING") "FOCUSING" else "PAUSED",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.veryHighColor
                    )
                }
            }
        }
    }
}

@Composable
private fun StartFocusSessionHeroCard(
    activeStatus: String,
    colorScheme: ThemeColorScheme,
    onCtaClick: () -> Unit
) {
    val (ctaTitle, ctaSubtitle, buttonLabel) = when (activeStatus) {
        "RUNNING" -> Triple("FOCUS SESSION IN PROGRESS", "Timer is active · Tap to view hourglass", "VIEW TIMER")
        "PAUSED" -> Triple("FOCUS SESSION PAUSED", "Timer is currently on hold · Tap to resume", "RESUME")
        else -> Triple("START FOCUS CYCLE", "25-minute standard interval · Sand physics", "START FOCUS")
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { onCtaClick() }
            .testTag("start_focus_session_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.2.dp,
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            colorScheme.veryHighColor.copy(alpha = 0.6f),
                            Color(0xFF2E2E2E)
                        )
                    ),
                    shape = RoundedCornerShape(18.dp)
                )
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            colorScheme.veryHighColor.copy(alpha = 0.08f),
                            Color.Transparent
                        ),
                        radius = 400f
                    )
                )
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(colorScheme.veryHighColor)
                        )
                        Text(
                            text = ctaTitle,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.veryHighColor,
                            letterSpacing = 0.6.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = ctaSubtitle,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.SansSerif,
                        color = SecondaryText
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Modern action pill button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(colorScheme.veryHighColor)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = buttonLabel,
                            tint = Color.Black,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = buttonLabel,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }
            }
        }
    }
}
