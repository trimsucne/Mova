package co.edu.unal.dadm.mova.presentation.viewmodel.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.unal.dadm.mova.domain.model.ActivityType
import co.edu.unal.dadm.mova.domain.model.TrackingState
import co.edu.unal.dadm.mova.domain.model.TrackingStatus
import co.edu.unal.dadm.mova.domain.repository.TrackingRepository
import co.edu.unal.dadm.mova.domain.usecase.LogActivityUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ActivityTrackUiState(
    val type: ActivityType,
    val tracking: TrackingState = TrackingState(),
    val saving: Boolean = false,
    val savedLogId: String? = null,
    /** Sesion demasiado corta (< MIN_SECONDS): se descarta sin guardar. */
    val discarded: Boolean = false,
    val permissionDenied: Boolean = false
) {
    val running: Boolean get() = tracking.status != TrackingStatus.Idle
}

sealed interface ActivityTrackEvent {
    data object StartRequested : ActivityTrackEvent
    data object PermissionDenied : ActivityTrackEvent
    data object PauseResumeClicked : ActivityTrackEvent
    data object FinishClicked : ActivityTrackEvent
    data object BackClicked : ActivityTrackEvent
}

class ActivityTrackViewModel(
    private val type: ActivityType,
    private val tracking: TrackingRepository,
    private val logActivity: LogActivityUseCase
) : ViewModel() {
    private val local = MutableStateFlow(ActivityTrackUiState(type))

    val state: StateFlow<ActivityTrackUiState> = combine(tracking.state, local) { t, l ->
        l.copy(tracking = t)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ActivityTrackUiState(type))

    fun onEvent(event: ActivityTrackEvent) {
        when (event) {
            ActivityTrackEvent.StartRequested -> {
                local.update { it.copy(permissionDenied = false) }
                if (tracking.state.value.status == TrackingStatus.Idle) tracking.start(type)
            }
            ActivityTrackEvent.PermissionDenied -> local.update { it.copy(permissionDenied = true) }
            ActivityTrackEvent.PauseResumeClicked ->
                if (tracking.state.value.status == TrackingStatus.Running) tracking.pause() else tracking.resume()
            ActivityTrackEvent.FinishClicked -> finish()
            ActivityTrackEvent.BackClicked -> Unit
        }
    }

    private fun finish() {
        if (local.value.saving) return
        val final = tracking.stop() ?: return
        if (final.elapsedSec < MIN_SECONDS) {
            local.update { it.copy(discarded = true) }
            return
        }
        local.update { it.copy(saving = true) }
        viewModelScope.launch {
            val log = logActivity(
                type = final.type ?: type,
                startedAt = final.startedAt,
                durationSec = final.elapsedSec,
                distanceMeters = final.distanceMeters,
                route = final.route
            )
            local.update { it.copy(saving = false, savedLogId = log.id) }
        }
    }

    companion object {
        const val MIN_SECONDS = 5
    }
}
