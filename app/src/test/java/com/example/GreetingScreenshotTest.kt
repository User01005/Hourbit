package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.focusgrid.domain.timer.SessionStatus
import com.example.focusgrid.domain.timer.TimerDisplayState
import com.example.focusgrid.ui.hourglass.AnimatedHourglass
import com.example.focusgrid.ui.theme.FocusGridTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

    @get:Rule val composeTestRule = createComposeRule()

    @Test
    fun hourglass_screenshot() {
        val state = TimerDisplayState(
            status = SessionStatus.RUNNING,
            elapsedMillis = 12 * 60 * 1000L,
            cycleNumber = 0,
            cycleProgress = 0.48f,
            orientationIsInverted = false,
            formattedTime = "12:00"
        )

        composeTestRule.setContent {
            FocusGridTheme {
                AnimatedHourglass(
                    timerState = state,
                    onSingleTap = {},
                    onDoubleTap = {}
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
    }
}
