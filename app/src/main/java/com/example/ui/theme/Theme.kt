package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = CyberBlue,
    secondary = CyberLime,
    tertiary = CyberGold,
    background = CyberObsidian,
    surface = CyberSteel,
    onPrimary = CyberObsidian,
    onSecondary = CyberObsidian,
    onTertiary = CyberObsidian,
    onBackground = CyberWhite,
    onSurface = CyberWhite,
    error = CyberRed
  )

private val LightColorScheme = DarkColorScheme // Default to dark for premium cyber-game aesthetic

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Force Dark mode for retro cyber game feel
  dynamicColor: Boolean = false, // Disable system dynamic tint to preserve custom theme integrity
  content: @Composable () -> Unit,
) {
  val colorScheme = DarkColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
