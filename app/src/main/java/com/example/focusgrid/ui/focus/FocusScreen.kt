package com.example.focusgrid.ui.focus

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.focusgrid.ui.hourglass.FocusHourglassSection
import com.example.focusgrid.ui.theme.CardSurface
import com.example.focusgrid.ui.theme.HighActivity
import com.example.focusgrid.ui.theme.MainBackground
import com.example.focusgrid.ui.theme.PrimaryText
import com.example.focusgrid.ui.theme.SecondaryText

@Composable
fun FocusScreen(
    viewModel: FocusViewModel,
    onBackToDashboard: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeSession by viewModel.activeSession.collectAsStateWithLifecycle()
    val timerState by viewModel.timerState.collectAsStateWithLifecycle()
    val todayTotalMillis by viewModel.todayTotalMillis.collectAsStateWithLifecycle()
    val colorScheme by viewModel.colorScheme.collectAsStateWithLifecycle()
    var showDiscardConfirmation by remember { mutableStateOf(false) }

    // System back returns to dashboard without finishing session
    BackHandler {
        onBackToDashboard()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MainBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Pixel Hourglass Component with interactive controls
        FocusHourglassSection(
            currentElapsedMillis = { activeSession.calculateElapsedMillis() },
            totalTodayMillis = todayTotalMillis,
            isRunning = activeSession.status == "RUNNING",
            colorScheme = colorScheme,
            onSingleTap = { viewModel.togglePauseResume() },
            onDoubleTap = { viewModel.finishSession(onBackToDashboard) },
            onFinishSession = { viewModel.finishSession(onBackToDashboard) },
            modifier = Modifier.fillMaxSize()
        )

        // Top Bar Overlay (Back Button & Discard Action)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF202020))
                    .border(1.dp, Color(0xFF323232), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = onBackToDashboard,
                    modifier = Modifier.testTag("back_to_dashboard_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Return to Dashboard",
                        tint = PrimaryText,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF202020))
                    .border(1.dp, Color(0xFF323232), RoundedCornerShape(10.dp))
            ) {
                TextButton(
                    onClick = { showDiscardConfirmation = true },
                    modifier = Modifier.testTag("discard_session_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            tint = SecondaryText,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "Discard",
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium,
                            color = SecondaryText
                        )
                    }
                }
            }
        }

        // Discard Confirmation Dialog
        if (showDiscardConfirmation) {
            AlertDialog(
                onDismissRequest = { showDiscardConfirmation = false },
                containerColor = CardSurface,
                shape = RoundedCornerShape(16.dp),
                title = {
                    Text(
                        text = "Discard Focus Session?",
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryText
                    )
                },
                text = {
                    Text(
                        text = "Are you sure you want to discard this focus session? The elapsed time will not be saved to your focus history.",
                        fontFamily = FontFamily.SansSerif,
                        color = SecondaryText
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDiscardConfirmation = false
                            viewModel.discardSession(onBackToDashboard)
                        }
                    ) {
                        Text("Discard", color = HighActivity, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDiscardConfirmation = false }) {
                        Text("Keep Focusing", color = PrimaryText)
                    }
                }
            )
        }
    }
}
