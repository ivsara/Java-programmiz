package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkIdeColorScheme = darkColorScheme(
    primary = IdeCyanPrimary,
    onPrimary = Color(0xFF00262E),
    primaryContainer = IdeCyanContainer,
    onPrimaryContainer = Color(0xFF9EEFFF),
    secondary = IdeAmberSecondary,
    onSecondary = Color(0xFF2B1B00),
    secondaryContainer = IdeAmberContainer,
    onSecondaryContainer = Color(0xFFFFDEA1),
    tertiary = IdeVioletTertiary,
    onTertiary = Color(0xFF23005C),
    tertiaryContainer = Color(0xFF371E73),
    onTertiaryContainer = Color(0xFFEADDFF),
    background = IdeObsidianBg,
    onBackground = IdeTextPrimaryDark,
    surface = IdeSurfaceDark,
    onSurface = IdeTextPrimaryDark,
    surfaceVariant = IdePanelDark,
    onSurfaceVariant = IdeTextSecondaryDark,
    outline = IdeBorderDark,
    error = IdeErrorRed,
    onError = Color.White
)

private val LightIdeColorScheme = lightColorScheme(
    primary = IdeBluePrimaryLight,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD1ECFF),
    onPrimaryContainer = Color(0xFF001E30),
    secondary = IdeAmberSecondaryLight,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDEA6),
    onSecondaryContainer = Color(0xFF2A1800),
    tertiary = IdeVioletTertiaryLight,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFE9DDFF),
    onTertiaryContainer = Color(0xFF22005D),
    background = IdeLightBg,
    onBackground = IdeTextPrimaryLight,
    surface = IdeSurfaceLight,
    onSurface = IdeTextPrimaryLight,
    surfaceVariant = IdePanelLight,
    onSurfaceVariant = IdeTextSecondaryLight,
    outline = IdeBorderLight,
    error = Color(0xFFB3261E),
    onError = Color.White
)

@Composable
fun UniversalJavaStudioTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkIdeColorScheme else LightIdeColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
