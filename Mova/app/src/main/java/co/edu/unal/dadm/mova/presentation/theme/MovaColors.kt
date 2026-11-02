package co.edu.unal.dadm.mova.presentation.theme

import androidx.compose.ui.graphics.Color

data class MovaColors(
    val primary: Color = MovaColor.PrimaryOrange,
    val onPrimary: Color = Color.White,
    val background: Color = MovaColor.BackgroundWhite,
    val surface: Color = MovaColor.CardLightGray,
    val surfaceBorder: Color = MovaColor.CardBorder,
    val textPrimary: Color = MovaColor.TextBlack,
    val textSecondary: Color = MovaColor.TextSecondary,
    val hydration: Color = MovaColor.HydrationBlue,
    val calorie: Color = MovaColor.CalorieRed,
    val score: Color = MovaColor.ScoreGreen
)

