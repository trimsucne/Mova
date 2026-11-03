package co.edu.unal.dadm.mova.presentation.screen.activity

import androidx.compose.ui.graphics.Color
import co.edu.unal.dadm.mova.domain.model.ActivityType
import co.edu.unal.dadm.mova.presentation.theme.MovaInk
import co.edu.unal.dadm.mova.presentation.theme.MovaOrange

/** Icono (emoji, sin dependencia de material-icons-extended) por tipo de actividad. */
fun ActivityType.emoji(): String = when (this) {
    ActivityType.Running -> "🏃"
    ActivityType.Jogging -> "🏃"
    ActivityType.Biking -> "🚴"
    ActivityType.Walking -> "🚶"
    ActivityType.Hiking -> "🥾"
    ActivityType.Swimming -> "🏊"
    ActivityType.Weightlifting -> "🏋️"
    ActivityType.Yoga -> "🧘"
    ActivityType.Basketball -> "🏀"
}

/** Colores de la leyenda de Activity Status. */
fun ActivityType.chartColor(): Color = when (this) {
    ActivityType.Running -> MovaInk
    ActivityType.Jogging -> MovaOrange
    ActivityType.Biking -> Color(0xFFEF4444)
    ActivityType.Weightlifting -> Color(0xFF2563EB)
    ActivityType.Yoga -> Color(0xFFDADADA)
    ActivityType.Walking -> Color(0xFF16A34A)
    ActivityType.Hiking -> Color(0xFF92400E)
    ActivityType.Swimming -> Color(0xFF06B6D4)
    ActivityType.Basketball -> Color(0xFF8B37FF)
}

fun ActivityType.chartTextColor(): Color = if (this == ActivityType.Yoga) MovaInk else Color.White
