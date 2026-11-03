package co.edu.unal.dadm.mova.presentation.viewmodel.places

import co.edu.unal.dadm.mova.domain.model.Gym

data class GymWithDistance(val gym: Gym, val distanceMeters: Double?)

data class NearbyGymsUiState(
    val loading: Boolean = true,
    val gyms: List<GymWithDistance> = emptyList(),
    val userLat: Double? = null,
    val userLng: Double? = null
)
