package co.edu.unal.dadm.mova.presentation.viewmodel.workout

import co.edu.unal.dadm.mova.domain.model.Workout
import co.edu.unal.dadm.mova.domain.model.WorkoutCategory

data class StrengthCategoryUiState(
    val loading: Boolean = true,
    val category: WorkoutCategory = WorkoutCategory.Strength,
    val workouts: List<Workout> = emptyList(),
    val error: String? = null
)
