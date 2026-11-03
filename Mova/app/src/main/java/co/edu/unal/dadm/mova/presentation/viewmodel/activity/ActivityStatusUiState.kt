package co.edu.unal.dadm.mova.presentation.viewmodel.activity

import co.edu.unal.dadm.mova.domain.model.ActivityLog
import co.edu.unal.dadm.mova.domain.model.ActivityType

data class ActivityStatusUiState(
    val loading: Boolean = true,
    val hoursByType: Map<ActivityType, Double> = emptyMap(),
    val recent: List<ActivityLog> = emptyList()
) {
    val isEmpty: Boolean get() = !loading && recent.isEmpty()
}
