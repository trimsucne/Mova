package co.edu.unal.dadm.mova.presentation.viewmodel.activity

import co.edu.unal.dadm.mova.domain.model.ActivityLog

data class ActivitySummaryUiState(
    val loading: Boolean = true,
    val log: ActivityLog? = null
)
