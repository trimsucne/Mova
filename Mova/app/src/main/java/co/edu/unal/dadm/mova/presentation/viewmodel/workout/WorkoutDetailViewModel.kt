package co.edu.unal.dadm.mova.presentation.viewmodel.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.unal.dadm.mova.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class WorkoutDetailViewModel(
    workoutId: String,
    repository: WorkoutRepository
) : ViewModel() {
    private val showDetails = MutableStateFlow(false)

    val state: StateFlow<WorkoutDetailUiState> = combine(
        repository.observeWorkout(workoutId), showDetails
    ) { workout, details ->
        WorkoutDetailUiState(loading = false, workout = workout, notFound = workout == null, showDetails = details)
    }
        .catch { emit(WorkoutDetailUiState(loading = false, error = it.message ?: "Error")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), WorkoutDetailUiState())

    fun onEvent(event: WorkoutDetailEvent) {
        when (event) {
            WorkoutDetailEvent.DetailsClicked -> showDetails.update { true }
            WorkoutDetailEvent.DetailsDismissed -> showDetails.update { false }
            else -> Unit // Start / Back los resuelve la Route (navegacion)
        }
    }
}
