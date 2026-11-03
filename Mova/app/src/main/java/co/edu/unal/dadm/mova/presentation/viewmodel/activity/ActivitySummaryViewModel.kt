package co.edu.unal.dadm.mova.presentation.viewmodel.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.unal.dadm.mova.domain.repository.ActivityRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class ActivitySummaryViewModel(logId: String, repository: ActivityRepository) : ViewModel() {
    val state: StateFlow<ActivitySummaryUiState> = repository.observe(logId)
        .map { ActivitySummaryUiState(loading = false, log = it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ActivitySummaryUiState())

    fun onEvent(event: ActivitySummaryEvent) = Unit
}
