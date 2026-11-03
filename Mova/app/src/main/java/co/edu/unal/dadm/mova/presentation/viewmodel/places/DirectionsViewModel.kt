package co.edu.unal.dadm.mova.presentation.viewmodel.places

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.unal.dadm.mova.core.common.GeoUtils
import co.edu.unal.dadm.mova.domain.repository.PlacesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlin.math.ceil

/**
 * Sin Directions API todavia: ruta en linea recta, ETA caminando (5 km/h) y rumbo aproximado.
 * La navegacion real se delega a Google Maps via [DirectionsEvent.OpenExternalMaps].
 */
class DirectionsViewModel(gymId: String, repository: PlacesRepository) : ViewModel() {
    private val location = MutableStateFlow<Pair<Double, Double>?>(null)

    val state: StateFlow<DirectionsUiState> = combine(repository.observeGym(gymId), location) { gym, loc ->
        if (gym == null) return@combine DirectionsUiState(loading = false)
        val distance = loc?.let { GeoUtils.distanceMeters(it.first, it.second, gym.lat, gym.lng) }
        val bearing = loc?.let { GeoUtils.bearingDegrees(it.first, it.second, gym.lat, gym.lng) }
        DirectionsUiState(
            loading = false,
            gym = gym,
            userLat = loc?.first,
            userLng = loc?.second,
            distanceMeters = distance,
            etaMinutes = distance?.let { ceil(it / WALK_METERS_PER_MIN).toInt().coerceAtLeast(1) },
            instruction = bearing?.let { "Head ${GeoUtils.compassLabel(it)}" }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DirectionsUiState())

    fun onEvent(event: DirectionsEvent) {
        if (event is DirectionsEvent.LocationUpdated) location.update { event.lat to event.lng }
    }

    private companion object {
        const val WALK_METERS_PER_MIN = 5_000.0 / 60.0
    }
}
