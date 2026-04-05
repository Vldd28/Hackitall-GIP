package org.example.project.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary              = WandrSteelBlue,
    onPrimary            = Color.White,
    primaryContainer     = WandrSteelBlueLight,
    onPrimaryContainer   = WandrSteelBlueDark,

    secondary            = WandrTeal,
    onSecondary          = Color.White,
    secondaryContainer   = WandrTeal.copy(alpha = 0.3f),
    onSecondaryContainer = WandrTealDark,

    tertiary             = WandrMint,
    onTertiary           = TextPrimary,
    tertiaryContainer    = WandrMint.copy(alpha = 0.3f),
    onTertiaryContainer  = WandrMintDark,

    background           = Background,
    onBackground         = TextPrimary,

    surface              = Surface,
    onSurface            = TextPrimary,
    surfaceVariant       = SurfaceVariant,
    onSurfaceVariant     = TextSecondary,

    error                = Error,
    onError              = Color.White,

    outline              = WandrSteelBlueLight,
    outlineVariant       = WandrCreamDark
)

private val DarkColorScheme = darkColorScheme(
    primary              = WandrTeal,
    onPrimary            = DarkBackground,
    primaryContainer     = WandrTealDark,
    onPrimaryContainer   = WandrTeal,

    secondary            = WandrMint,
    onSecondary          = DarkBackground,
    secondaryContainer   = WandrMintDark,
    onSecondaryContainer = WandrMint,

    tertiary             = WandrCream,
    onTertiary           = DarkBackground,
    tertiaryContainer    = WandrCreamDark,
    onTertiaryContainer  = WandrCream,

    background           = DarkBackground,
    onBackground         = WandrCream,

    surface              = DarkSurface,
    onSurface            = WandrCream,
    surfaceVariant       = DarkSurfaceVariant,
    onSurfaceVariant     = WandrSteelBlueLight,

    error                = Error,
    onError              = Color.White,

    outline              = WandrSteelBlue,
    outlineVariant       = DarkSurfaceVariant
)

@Composable
fun WandrTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = AppTypography,
        content = content
    )
}
