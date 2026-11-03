package co.edu.unal.dadm.mova.presentation.viewmodel.places

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.unal.dadm.mova.core.common.GeoUtils
import co.edu.unal.dadm.mova.domain.model.RoutePlan
import co.edu.unal.dadm.mova.domain.repository.DirectionsRepository
import co.edu.unal.dadm.mova.domain.repository.PlacesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.ceil

/**
 * Ruta a pie por calles via [DirectionsRepository] (OSRM, sin API key). Si el servicio no responde,
 * cae a linea recta con ETA caminando (5 km/h) y rumbo aproximado. La navegacion paso a paso real se
 * delega a Google Maps via [DirectionsEvent.OpenExternalMaps].
 */
class DirectionsViewModel(
    private val gymId: String,
    private val places: PlacesRepository,
    private val directions: DirectionsRepository
) : ViewModel() {
    private val location = MutableStateFlow<Pair<Double, Double>?>(null)
    private val plan = MutableStateFlow<RoutePlan?>(null)
    private val routeLoading = MutableStateFlow(false)
    private var routeOrigin: Pair<Double, Double>? = null

    val state: StateFlow<DirectionsUiState> = combine(
        places.observeGym(gymId), location, plan, routeLoading
    ) { gym, loc, route, loadingRoute ->
        if (gym == null) return@combine DirectionsUiState(loading = false)
        val straight = loc?.let { GeoUtils.distanceMeters(it.first, it.second, gym.lat, gym.lng) }
        val distance = route?.distanceMeters ?: straight
        val etaMinutes = when {
            route != null -> ceil(route.durationSec / 60.0).toInt().coerceAtLeast(1)
            straight != null -> ceil(straight / WALK_METERS_PER_MIN).toInt().coerceAtLeast(1)
            else -> null
        }
        val instruction = when {
            route != null -> routeInstruction(route)
            loc != null -> "Head ${GeoUtils.compassLabel(GeoUtils.bearingDegrees(loc.first, loc.second, gym.lat, gym.lng))}"
            else -> null
        }
        DirectionsUiState(
            loading = false,
            gym = gym,
            userLat = loc?.first,
            userLng = loc?.second,
            distanceMeters = distance,
            etaMinutes = etaMinutes,
            instruction = instruction,
            route = route?.points.orEmpty(),
            routeLoading = loadingRoute
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DirectionsUiState())

    fun onEvent(event: DirectionsEvent) {
        if (event is DirectionsEvent.LocationUpdated) onLocation(event.lat, event.lng)
    }

    private fun onLocation(lat: Double, lng: Double) {
        location.update { lat to lng }
        val last = routeOrigin
        // Pide ruta la primera vez y cuando el usuario se movio mas de REROUTE_METERS desde el origen anterior.
        if (last != null && GeoUtils.distanceMeters(last.first, last.second, lat, lng) < REROUTE_METERS) return
        routeOrigin = lat to lng
        viewModelScope.launch {
            val gym = places.observeGym(gymId).filterNotNull().first()
            routeLoading.value = true
            val result = directions.walkingRoute(lat, lng, gym.lat, gym.lng)
            // Si falla se conserva la ultima ruta buena (o linea recta si nunca hubo).
            if (result != null) plan.value = result
            routeLoading.value = false
        }
    }

    private fun routeInstruction(route: RoutePlan): String? {
        val steps = route.steps
        val next = steps.getOrNull(1) ?: return steps.firstOrNull()?.instruction
        return "In ${GeoUtils.formatDistance(steps[0].distanceMeters)}: ${next.instruction}"
    }

    private companion object {
        const val WALK_METERS_PER_MIN = 5_000.0 / 60.0
        const val REROUTE_METERS = 75.0
    }
}
