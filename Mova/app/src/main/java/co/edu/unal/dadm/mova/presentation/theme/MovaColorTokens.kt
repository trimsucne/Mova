package co.edu.unal.dadm.mova.presentation.theme

import androidx.compose.ui.graphics.Color

/**
 * Tokens de color con nombre semántico que usan Home, Notificaciones y Uplift Score (PR #5).
 * Reutilizan la paleta de MovaColor.kt donde existe un equivalente; el resto son tintes claros
 * tomados del diseño original de PR #5.
 */
object MovaColor {
    val PrimaryOrange = MovaOrange
    val PrimaryOrangeDark = MovaOrangeDark
    val PrimaryOrangeLight = MovaOrangeSoft

    val BackgroundWhite = Color.White
    val CardLightGray = MovaField
    val CardBorder = MovaBorder

    val TextBlack = MovaInk
    val TextDarkGray = Color(0xFF3A3B40)
    val TextSecondary = MovaMuted

    val HydrationBlue = MovaBlueIcon
    val HydrationBlueLight = Color(0xFFEFF6FF)

    val CalorieRed = MovaError
    val CalorieLight = MovaErrorContainer

    val ScoreGreen = Color(0xFF10B981)
    val ScoreGreenLight = MovaSuccessGreenBg
}
