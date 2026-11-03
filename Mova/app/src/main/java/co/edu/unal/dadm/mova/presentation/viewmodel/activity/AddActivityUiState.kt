package co.edu.unal.dadm.mova.presentation.viewmodel.activity

import co.edu.unal.dadm.mova.domain.model.ActivityType

data class AddActivityUiState(
    val types: List<ActivityType> = ActivityType.entries,
    val selected: ActivityType? = null
) {
    val canContinue: Boolean get() = selected != null
}
