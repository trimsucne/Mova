package co.edu.unal.dadm.mova.presentation.viewmodel.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.unal.dadm.mova.domain.repository.ActivityRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class ActivityStatusViewModel(repository: ActivityRepository) : ViewModel() {
    val state: StateFlow<ActivityStatusUiState> = combine(
        repository.observeHoursByType(), repository.observeAll()
    ) { hours, all ->
        ActivityStatusUiState(loading = false, hoursByType = hours, recent = all)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ActivityStatusUiState())

    fun onEvent(event: ActivityStatusEvent) = Unit
}
