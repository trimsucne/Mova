package co.edu.unal.dadm.mova.domain.model

data class RoutePoint(
    val lat: Double,
    val lng: Double,
    val timestamp: Long
)

/**
 * Actividad registrada. El constructor principal es el del modulo de Actividades; el constructor
 * secundario (id, title, durationMinutes, distanceKm, caloriesBurned) conserva el contrato que usa Home.
 */
data class ActivityLog(
    val id: String,
    val type: ActivityType,
    val startedAt: Long,
    val durationSec: Int,
    val distanceMeters: Double,
    val kcal: Int,
    val avgBpm: Int? = null,
    val route: List<RoutePoint> = emptyList(),
    /** Titulo personalizado; si es nulo se usa la etiqueta del tipo. */
    val customTitle: String? = null
) {
    constructor(
        id: String,
        title: String,
        durationMinutes: Int,
        distanceKm: Double,
        caloriesBurned: Int
    ) : this(
        id = id,
        type = ActivityType.Walking,
        startedAt = 0L,
        durationSec = durationMinutes * 60,
        distanceMeters = distanceKm * 1000.0,
        kcal = caloriesBurned,
        customTitle = title
    )

    val hours: Double get() = durationSec / 3600.0

    /** Alias usados por la UI de Home. */
    val title: String get() = customTitle ?: type.label
    val durationMinutes: Int get() = durationSec / 60
    val distanceKm: Double get() = distanceMeters / 1000.0
    val caloriesBurned: Int get() = kcal
}
