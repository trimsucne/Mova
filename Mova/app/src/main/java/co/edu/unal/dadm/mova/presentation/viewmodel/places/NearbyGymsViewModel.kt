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

class NearbyGymsViewModel(repository: PlacesRepository) : ViewModel() {
    private val location = MutableStateFlow<Pair<Double, Double>?>(null)

    val state: StateFlow<NearbyGymsUiState> = combine(repository.observeGyms(), location) { gyms, loc ->
        val items = gyms.map { g ->
            GymWithDistance(g, loc?.let { GeoUtils.distanceMeters(it.first, it.second, g.lat, g.lng) })
        }.sortedBy { it.distanceMeters ?: Double.MAX_VALUE }
        NearbyGymsUiState(loading = false, gyms = items, userLat = loc?.first, userLng = loc?.second)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), NearbyGymsUiState())

    fun onEvent(event: NearbyGymsEvent) {
        if (event is NearbyGymsEvent.LocationUpdated) location.update { event.lat to event.lng }
    }
}
