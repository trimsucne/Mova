package co.edu.unal.dadm.mova.presentation.viewmodel.workout

sealed interface WorkoutSessionEvent {
    /** Pulso de 1 segundo emitido por la Route mientras la pantalla esta visible. */
    data object Tick : WorkoutSessionEvent
    data object CompleteSet : WorkoutSessionEvent
    data object SkipRest : WorkoutSessionEvent
    data object TogglePause : WorkoutSessionEvent
    data object FinishClicked : WorkoutSessionEvent
    data object BackClicked : WorkoutSessionEvent
}
