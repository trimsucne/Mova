package co.edu.unal.dadm.mova.presentation.viewmodel.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.unal.dadm.mova.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class WorkoutSummaryViewModel(
    sessionId: String,
    repository: WorkoutRepository
) : ViewModel() {
    val state: StateFlow<WorkoutSummaryUiState> = repository.observeSession(sessionId)
        .map { WorkoutSummaryUiState(loading = false, session = it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), WorkoutSummaryUiState())

    fun onEvent(event: WorkoutSummaryEvent) = Unit
}
