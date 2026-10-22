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

private val DarkColorScheme = darkColorScheme(
    primary = RoyalBlue,
    secondary = DarkNavy,
    tertiary = AccentOrange,
    background = NavyDarkest,
    surface = DarkNavy,
    onPrimary = PureWhite,
    onSecondary = PureWhite,
    onTertiary = PureWhite,
    onBackground = PureWhite,
    onSurface = PureWhite
)

private val LightColorScheme = lightColorScheme(
    primary = RoyalBlue,
    onPrimary = PureWhite,
    primaryContainer = WaveLightBlue,
    onPrimaryContainer = DarkNavy,
    secondary = DarkNavy,
    onSecondary = PureWhite,
    secondaryContainer = WaveSubtleBlue,
    onSecondaryContainer = DarkNavy,
    tertiary = AccentOrange,
    onTertiary = PureWhite,
    background = PureWhite,
    onBackground = TextPrimaryNavy,
    surface = PureWhite,
    onSurface = TextPrimaryNavy,
    outline = DividerColor,
    outlineVariant = WaveLightBlue
)

@Composable
fun GetVidyaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Keep brand identity stable by default
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
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

// Retain alias for backward compatibility
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    GetVidyaTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
}
