package co.edu.unal.dadm.mova.presentation.viewmodel.activity

import co.edu.unal.dadm.mova.domain.model.ActivityType

sealed interface AddActivityEvent {
    data class TypeSelected(val type: ActivityType) : AddActivityEvent
    data object ContinueClicked : AddActivityEvent
    data object BackClicked : AddActivityEvent
}
