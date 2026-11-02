package co.edu.unal.dadm.mova.presentation.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

val LightColorScheme = lightColorScheme(
    primary = MovaOrange,
    onPrimary = Color.White,
    primaryContainer = MovaOrangeSoft,
    onPrimaryContainer = MovaOrangeDark,
    secondary = MovaInk,
    onSecondary = Color.White,
    background = Color.White,
    onBackground = MovaInk,
    surface = Color.White,
    onSurface = MovaInk,
    surfaceVariant = MovaField,
    onSurfaceVariant = MovaMuted,
    outline = MovaBorder,
    error = MovaError,
    errorContainer = MovaErrorContainer,
    onError = Color.White
)

val DarkColorScheme = darkColorScheme(
    primary = MovaOrange,
    onPrimary = Color.White,
    primaryContainer = MovaOrangeDark,
    onPrimaryContainer = Color.White,
    secondary = Color.White,
    onSecondary = MovaInk,
    background = MovaInk,
    onBackground = Color.White,
    surface = Color(0xFF1E1F24),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF2B2C32),
    onSurfaceVariant = Color(0xFFA0A0A5),
    outline = Color(0xFF3F4046),
    error = MovaError,
    errorContainer = Color(0xFF5C1D1D),
    onError = Color.White
)
