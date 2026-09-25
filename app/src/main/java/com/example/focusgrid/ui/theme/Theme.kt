package com.example.focusgrid.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = HighActivity,
    onPrimary = PrimaryText,
    primaryContainer = MediumActivity,
    onPrimaryContainer = PrimaryText,
    secondary = LowActivity,
    onSecondary = PrimaryText,
    background = MainBackground,
    onBackground = PrimaryText,
    surface = CardSurface,
    onSurface = PrimaryText,
    surfaceVariant = RaisedSurface,
    onSurfaceVariant = SecondaryText,
    outline = DividerLines,
    outlineVariant = InactiveCell
)

@Composable
fun FocusGridTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
