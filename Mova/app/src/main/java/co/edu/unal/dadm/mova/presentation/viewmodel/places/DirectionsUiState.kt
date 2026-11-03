package co.edu.unal.dadm.mova.presentation.viewmodel.places

import co.edu.unal.dadm.mova.domain.model.Gym
import co.edu.unal.dadm.mova.domain.model.GeoPoint

data class DirectionsUiState(
    val loading: Boolean = true,
    val gym: Gym? = null,
    val userLat: Double? = null,
    val userLng: Double? = null,
    val distanceMeters: Double? = null,
    val etaMinutes: Int? = null,
    /** Proxima instruccion, p. ej. "In 120 m: Turn right onto Calle 26". */
    val instruction: String? = null,
    /** Ruta por calles (OSRM). Vacia si aun no llega o el servicio fallo: la UI dibuja linea recta. */
    val route: List<GeoPoint> = emptyList(),
    val routeLoading: Boolean = false
)
