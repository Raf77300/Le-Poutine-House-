package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = CanadianRed,
    secondary = GoldenPotato,
    tertiary = PotatoBeige,
    background = PrimaryBlack,
    surface = Color(0xFF262626),
    onPrimary = White,
    onSecondary = PrimaryBlack,
    onTertiary = PrimaryBlack,
    onBackground = CreamBackground,
    onSurface = CreamBackground
  )

private val LightColorScheme =
  lightColorScheme(
    primary = CanadianRed,
    secondary = GoldenPotato,
    tertiary = PotatoBeige,
    background = CreamBackground,
    surface = White,
    onPrimary = White,
    onSecondary = PrimaryBlack,
    onTertiary = PrimaryBlack,
    onBackground = PrimaryBlack,
    onSurface = PrimaryBlack
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Retain branding colors rather than dynamic wallpaper colors
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
