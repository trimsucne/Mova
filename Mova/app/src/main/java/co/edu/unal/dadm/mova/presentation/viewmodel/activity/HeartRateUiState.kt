package co.edu.unal.dadm.mova.presentation.viewmodel.activity

import co.edu.unal.dadm.mova.domain.model.VitalSample

data class HeartRateUiState(
    val sample: VitalSample? = null,
    /** Etiqueta de lo que el usuario esta haciendo ("Resting" si no hay tracking). */
    val currentActivity: String = "Resting"
)
