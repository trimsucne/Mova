package co.edu.unal.dadm.mova.domain.model

data class RoutePoint(
    val lat: Double,
    val lng: Double,
    val timestamp: Long
)

data class ActivityLog(
    val id: String,
    val type: ActivityType,
    val startedAt: Long,
    val durationSec: Int,
    val distanceMeters: Double,
    val kcal: Int,
    val avgBpm: Int? = null,
    val route: List<RoutePoint> = emptyList()
) {
    val hours: Double get() = durationSec / 3600.0
}
