package com.example.focusgrid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.focusgrid.ui.focus.FocusScreen
import com.example.focusgrid.ui.focus.FocusViewModel
import com.example.focusgrid.ui.home.HomeScreen
import com.example.focusgrid.ui.home.HomeViewModel
import com.example.focusgrid.ui.theme.FocusGridTheme
import com.example.focusgrid.ui.theme.MainBackground

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as FocusGridApplication
        val repository = app.repository
        val timerEngine = app.timerEngine

        setContent {
            FocusGridTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MainBackground
                ) {
                    val homeViewModel: HomeViewModel = viewModel(
                        factory = HomeViewModel.Factory(repository)
                    )
                    val focusViewModel: FocusViewModel = viewModel(
                        factory = FocusViewModel.Factory(repository, timerEngine)
                    )

                    val activeSession by homeViewModel.activeSession.collectAsStateWithLifecycle()

                    var currentScreen by remember(activeSession.status) {
                        mutableStateOf(
                            if (activeSession.isSessionActive) "FOCUS" else "HOME"
                        )
                    }

                    if (currentScreen == "FOCUS") {
                        FocusScreen(
                            viewModel = focusViewModel,
                            onBackToDashboard = { currentScreen = "HOME" }
                        )
                    } else {
                        HomeScreen(
                            viewModel = homeViewModel,
                            onOpenFocusTimer = { currentScreen = "FOCUS" }
                        )
                    }
                }
            }
        }
    }
}
