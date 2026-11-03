package co.edu.unal.dadm.mova.presentation.viewmodel.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.unal.dadm.mova.core.health.HealthDataSource
import co.edu.unal.dadm.mova.domain.model.TrackingStatus
import co.edu.unal.dadm.mova.domain.repository.TrackingRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class HeartRateViewModel(
    health: HealthDataSource,
    tracking: TrackingRepository
) : ViewModel() {
    val state: StateFlow<HeartRateUiState> = combine(health.observeVitals(), tracking.state) { sample, t ->
        HeartRateUiState(
            sample = sample,
            currentActivity = if (t.status != TrackingStatus.Idle) t.type?.label ?: "Resting" else "Resting"
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HeartRateUiState())

    fun onEvent(event: HeartRateEvent) = Unit
}
