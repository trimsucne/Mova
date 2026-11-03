package co.edu.unal.dadm.mova.presentation.viewmodel.activity

sealed interface HeartRateEvent {
    data object BackClicked : HeartRateEvent
}
