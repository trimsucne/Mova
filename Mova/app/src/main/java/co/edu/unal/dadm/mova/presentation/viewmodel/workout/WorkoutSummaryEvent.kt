package co.edu.unal.dadm.mova.presentation.viewmodel.workout

sealed interface WorkoutSummaryEvent {
    data object CompleteClicked : WorkoutSummaryEvent
}
