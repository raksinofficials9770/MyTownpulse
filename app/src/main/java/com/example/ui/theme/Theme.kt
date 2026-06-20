package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = SunsetOrange,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFFE65100),
    onPrimaryContainer = Color.White,
    secondary = WarmAmber,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFFFF8F00),
    onSecondaryContainer = Color.White,
    tertiary = RiverBlue,
    onTertiary = Color.White,
    background = SlateDarkBg,
    onBackground = OnBackgroundDark,
    surface = CharcoalSurface,
    onSurface = OnBackgroundDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextMutedDark,
    error = CrimsonRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = BentoPurple,
    onPrimary = Color.White,
    primaryContainer = BentoLavenderContainer,
    onPrimaryContainer = Color(0xFF21005D),
    secondary = BentoLavenderContainer,
    onSecondary = Color(0xFF1D192B),
    secondaryContainer = BentoSecondaryContainer,
    onSecondaryContainer = Color(0xFF1D192B),
    tertiary = RiverBlue,
    onTertiary = Color.White,
    background = BentoBackgroundLight,
    onBackground = OnBackgroundLight,
    surface = Color.White,
    onSurface = OnBackgroundLight,
    surfaceVariant = BentoSecondaryContainer,
    onSurfaceVariant = Color(0xFF49454F),
    error = CrimsonRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Keep dynamicColor false to enforce our beautiful custom "Campfire & Sunset" brand personality,
    // which looks infinitely superior to flat system gray-blue layers!
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
