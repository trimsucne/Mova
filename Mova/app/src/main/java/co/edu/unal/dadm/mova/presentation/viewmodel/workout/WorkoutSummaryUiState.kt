package co.edu.unal.dadm.mova.presentation.viewmodel.workout

import co.edu.unal.dadm.mova.domain.model.WorkoutSessionLog

data class WorkoutSummaryUiState(
    val loading: Boolean = true,
    val session: WorkoutSessionLog? = null
)
