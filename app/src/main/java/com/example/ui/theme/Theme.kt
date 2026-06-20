package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80,
    background = CarbonDark,
    surface = DarkSurface,
    onBackground = Color.White,
    onSurface = Color.White,
    primaryContainer = DarkSurfaceLighter,
    onPrimaryContainer = Color.White
  )

private val LightColorScheme =
  lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,
    background = LightBg,
    surface = LightSurface,
    onBackground = LightTextPrimary,
    onSurface = LightTextPrimary,
    primaryContainer = Color.White,
    onPrimaryContainer = LightTextPrimary
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // default to cyber dark mode for excellent visual look
  dynamicColor: Boolean = false, // disable dynamic color so our beautiful custom theme shines
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
