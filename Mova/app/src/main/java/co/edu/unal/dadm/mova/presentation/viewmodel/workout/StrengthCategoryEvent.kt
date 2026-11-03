package co.edu.unal.dadm.mova.presentation.viewmodel.workout

sealed interface StrengthCategoryEvent {
    data class WorkoutClicked(val workoutId: String) : StrengthCategoryEvent
    data object BackClicked : StrengthCategoryEvent
}
