package co.edu.unal.dadm.mova.domain.model

/**
 * Actividad registrada en el día (lo que muestra la tarjeta de actividades de Home, PR #5).
 *
 * Temporary fake until the Activity module is implemented.
 */
data class ActivityLog(
    val id: String,
    val title: String,
    val durationMinutes: Int,
    val distanceKm: Double,
    val caloriesBurned: Int
)
