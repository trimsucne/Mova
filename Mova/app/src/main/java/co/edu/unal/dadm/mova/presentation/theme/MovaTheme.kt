package co.edu.unal.dadm.mova.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = MovaColor.PrimaryOrange,
    onPrimary = Color.White,
    primaryContainer = MovaColor.PrimaryOrangeLight,
    onPrimaryContainer = MovaColor.PrimaryOrangeDark,
    background = MovaColor.BackgroundWhite,
    onBackground = MovaColor.TextBlack,
    surface = MovaColor.CardLightGray,
    onSurface = MovaColor.TextBlack,
    surfaceVariant = MovaColor.CardBorder,
    onSurfaceVariant = MovaColor.TextSecondary
)

@Composable
fun MovaTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = MovaTypography,
        shapes = MaterialTheme.shapes,
        content = content
    )
}

