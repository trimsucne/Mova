package co.edu.unal.dadm.mova.presentation.viewmodel.workout

import co.edu.unal.dadm.mova.domain.model.Workout

data class WorkoutDetailUiState(
    val loading: Boolean = true,
    val workout: Workout? = null,
    val notFound: Boolean = false,
    val showDetails: Boolean = false,
    val error: String? = null
)
