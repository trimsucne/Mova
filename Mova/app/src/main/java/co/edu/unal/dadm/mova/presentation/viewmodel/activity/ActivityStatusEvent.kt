package co.edu.unal.dadm.mova.presentation.viewmodel.activity

sealed interface ActivityStatusEvent {
    data object AddClicked : ActivityStatusEvent
    data class ActivityClicked(val logId: String) : ActivityStatusEvent
    data object BackClicked : ActivityStatusEvent
}
