package co.edu.unal.dadm.mova.domain.model

import java.time.LocalDate

/**
 * Foto diaria del Uplift Score y sus componentes (PR #5: Home y pantalla Uplift Score).
 * dayOfWeek es la etiqueta corta del día ("Mon".."Sun") que usa la gráfica semanal.
 *
 * Temporary fake until the Progress/Uplift module is implemented.
 */
data class ScoreSnapshot(
    val date: LocalDate,
    val dayOfWeek: String,
    val score: Int,
    val healthStatus: String,
    val strength: Int,
    val agility: Int,
    val endurance: Int
)
