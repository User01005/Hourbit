package com.example.focusgrid.domain.theme

import androidx.compose.ui.graphics.Color

enum class ThemeColorScheme(
    val id: String,
    val displayName: String,
    val colorLowHex: String,
    val colorMediumHex: String,
    val colorHighHex: String,
    val colorVeryHighHex: String,
    val colorOutlineHex: String,
    val inactiveCellHex: String = "#323232",
    val outsideMonthHex: String = "#4C323232",
    val validZeroHex: String = "#87323232"
) {
    RED(
        id = "red",
        displayName = "Crimson Red",
        colorLowHex = "#441D1D",
        colorMediumHex = "#7A2020",
        colorHighHex = "#B81B1B",
        colorVeryHighHex = "#FF1414",
        colorOutlineHex = "#FF6B6B"
    ),
    YELLOW(
        id = "yellow",
        displayName = "Amber Gold",
        colorLowHex = "#3D3119",
        colorMediumHex = "#684B17",
        colorHighHex = "#866115",
        colorVeryHighHex = "#FFAF0E",
        colorOutlineHex = "#FFD066"
    ),
    BLUE(
        id = "blue",
        displayName = "Electric Blue",
        colorLowHex = "#2F3B49",
        colorMediumHex = "#374A5E",
        colorHighHex = "#445B76",
        colorVeryHighHex = "#83BAFA",
        colorOutlineHex = "#B5D7FD"
    ),
    GREEN(
        id = "green",
        displayName = "Forest Green",
        colorLowHex = "#284022",
        colorMediumHex = "#3C6033",
        colorHighHex = "#508043",
        colorVeryHighHex = "#A0FF86",
        colorOutlineHex = "#C4FF9E"
    ),
    NEON_GREEN(
        id = "neon_green",
        displayName = "Neon Green",
        colorLowHex = "#3398FF53",
        colorMediumHex = "#6698FF53",
        colorHighHex = "#9C98FF53",
        colorVeryHighHex = "#FF98FF53",
        colorOutlineHex = "#98FF53"
    );

    val lowColor: Color get() = Color(android.graphics.Color.parseColor(colorLowHex))
    val mediumColor: Color get() = Color(android.graphics.Color.parseColor(colorMediumHex))
    val highColor: Color get() = Color(android.graphics.Color.parseColor(colorHighHex))
    val veryHighColor: Color get() = Color(android.graphics.Color.parseColor(colorVeryHighHex))
    val outlineColor: Color get() = Color(android.graphics.Color.parseColor(colorOutlineHex))
    val inactiveColor: Color get() = Color(android.graphics.Color.parseColor(inactiveCellHex))

    companion object {
        fun fromId(id: String?): ThemeColorScheme {
            return entries.find { it.id.equals(id, ignoreCase = true) } ?: RED
        }
    }
}
