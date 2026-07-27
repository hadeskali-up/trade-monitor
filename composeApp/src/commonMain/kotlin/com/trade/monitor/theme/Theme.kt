package com.trade.monitor.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF1E3A5F),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD6E4FF),
    onPrimaryContainer = Color(0xFF001B3D),
    secondary = Color(0xFF3A5F8A),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD6E4FF),
    onSecondaryContainer = Color(0xFF001B3D),
    tertiary = Color(0xFF4A90D9),
    tertiaryContainer = Color(0xFFE8F0FE),
    onTertiaryContainer = Color(0xFF001B3D),
    background = Color.White,
    onBackground = Color(0xFF1A1C1E),
    surface = Color.White,
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = Color(0xFFF0F4FA),
    onSurfaceVariant = Color(0xFF44474E),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    outline = Color(0xFFD0D5DD)
)

val ProfitGreen = Color(0xFF4CAF50)
val LossRed = Color(0xFFEF5350)
val NeutralGray = Color(0xFF9E9E9E)

@Composable
fun TradeMonitorTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColors,
        content = content
    )
}
