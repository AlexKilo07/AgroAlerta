package com.example.agroalerta.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = AgroGreenPrimary,
    background = AgroBackground,
    surface = AgroCardBackground,
    onPrimary = AgroCardBackground,
    onBackground = AgroTextDark,
    onSurface = AgroTextDark
)

@Composable
fun AgroAlertaTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}