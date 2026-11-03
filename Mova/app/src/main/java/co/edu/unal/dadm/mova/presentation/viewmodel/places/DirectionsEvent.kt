package co.edu.unal.dadm.mova.presentation.viewmodel.places

sealed interface DirectionsEvent {
    data class LocationUpdated(val lat: Double, val lng: Double) : DirectionsEvent
    data object OpenExternalMaps : DirectionsEvent
    data object CloseClicked : DirectionsEvent
}
