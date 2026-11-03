package co.edu.unal.dadm.mova.domain.model

data class GeoPoint(val lat: Double, val lng: Double)

/** Un paso de la ruta, p. ej. "Turn right onto Calle 26" a [distanceMeters] del paso anterior. */
data class RouteStep(val instruction: String, val distanceMeters: Double)

/** Ruta calculada por calles entre dos puntos. */
data class RoutePlan(
    val points: List<GeoPoint>,
    val distanceMeters: Double,
    val durationSec: Double,
    val steps: List<RouteStep>
)
