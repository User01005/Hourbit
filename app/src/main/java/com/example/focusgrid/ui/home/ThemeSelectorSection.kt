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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.focusgrid.domain.theme.ThemeColorScheme
import com.example.focusgrid.ui.theme.CardSurface
import com.example.focusgrid.ui.theme.PrimaryText
import com.example.focusgrid.ui.theme.SecondaryText

@Composable
fun ThemeSelectorSection(
    currentTheme: ThemeColorScheme,
    onSelectTheme: (ThemeColorScheme) -> Unit,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag("theme_settings_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFF262626), RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(currentTheme.veryHighColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "Palette",
                            tint = currentTheme.veryHighColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "COLOR THEME",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = SecondaryText,
                            letterSpacing = 0.6.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(currentTheme.veryHighColor)
                            )
                            Text(
                                text = currentTheme.displayName,
                                fontSize = 15.sp,
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.SemiBold,
                                color = PrimaryText
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onToggleExpand,
                    modifier = Modifier.testTag("toggle_theme_settings_button")
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Toggle Color Themes",
                        tint = SecondaryText,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Select active color scheme for app and widgets:",
                        fontSize = 12.sp,
                        fontFamily = FontFamily.SansSerif,
                        color = SecondaryText
                    )

                    ThemeColorScheme.entries.forEach { theme ->
                        val isSelected = (theme == currentTheme)
                        ThemeOptionRow(
                            theme = theme,
                            isSelected = isSelected,
                            onSelect = { onSelectTheme(theme) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ThemeOptionRow(
    theme: ThemeColorScheme,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) Color(0xFF252525) else Color(0xFF1E1E1E))
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) theme.veryHighColor else Color(0xFF2E2E2E),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onSelect() }
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .testTag("theme_option_${theme.id}"),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 4-step progressive intensity palette preview
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(
                    modifier = Modifier
                        .size(15.dp)
                        .clip(RoundedCornerShape(3.5.dp))
                        .background(theme.lowColor)
                )
                Box(
                    modifier = Modifier
                        .size(15.dp)
                        .clip(RoundedCornerShape(3.5.dp))
                        .background(theme.mediumColor)
                )
                Box(
                    modifier = Modifier
                        .size(15.dp)
                        .clip(RoundedCornerShape(3.5.dp))
                        .background(theme.highColor)
                )
                Box(
                    modifier = Modifier
                        .size(15.dp)
                        .clip(RoundedCornerShape(3.5.dp))
                        .background(theme.veryHighColor)
                )
            }

            Text(
                text = theme.displayName,
                fontSize = 14.sp,
                fontFamily = FontFamily.SansSerif,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) PrimaryText else SecondaryText
            )
        }

        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(theme.veryHighColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected Theme",
                    tint = theme.veryHighColor,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}
