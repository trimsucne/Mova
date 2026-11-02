package co.edu.unal.dadm.mova.presentation.viewmodel.home

sealed interface HomeEvent {
    data class SearchQueryChanged(val query: String) : HomeEvent
    object PerformSearch : HomeEvent
    object OpenNotifications : HomeEvent
    object OpenHydration : HomeEvent
    object OpenScore : HomeEvent
    object OpenNutrition : HomeEvent
    object OpenCoach : HomeEvent
    data class SelectWorkout(val workoutId: String) : HomeEvent
}

