package co.edu.unal.dadm.mova.presentation.viewmodel.places

import co.edu.unal.dadm.mova.domain.model.Gym

data class DirectionsUiState(
    val loading: Boolean = true,
    val gym: Gym? = null,
    val userLat: Double? = null,
    val userLng: Double? = null,
    val distanceMeters: Double? = null,
    val etaMinutes: Int? = null,
    /** Instruccion aproximada, p. ej. "Head north-east". */
    val instruction: String? = null
)
