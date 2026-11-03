package co.edu.unal.dadm.mova.presentation.viewmodel.workout

sealed interface WorkoutDetailEvent {
    data object StartClicked : WorkoutDetailEvent
    data object DetailsClicked : WorkoutDetailEvent
    data object DetailsDismissed : WorkoutDetailEvent
    data object BackClicked : WorkoutDetailEvent
}
