package co.edu.unal.dadm.mova.domain.model

enum class TrackingStatus { Idle, Running, Paused }

data class TrackingState(
    val status: TrackingStatus = TrackingStatus.Idle,
    val type: ActivityType? = null,
    val startedAt: Long = 0L,
    val elapsedSec: Int = 0,
    val distanceMeters: Double = 0.0,
    val route: List<RoutePoint> = emptyList()
) {
    /** Ritmo en min/km, o null si aun no hay distancia suficiente. */
    val paceMinPerKm: Double?
        get() = if (distanceMeters >= 50) (elapsedSec / 60.0) / (distanceMeters / 1000.0) else null
}
