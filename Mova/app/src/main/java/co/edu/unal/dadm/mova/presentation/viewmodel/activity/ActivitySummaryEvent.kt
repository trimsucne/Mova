package co.edu.unal.dadm.mova.presentation.viewmodel.activity

sealed interface ActivitySummaryEvent {
    data object DoneClicked : ActivitySummaryEvent
    data object BackClicked : ActivitySummaryEvent
}
