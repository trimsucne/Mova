package co.edu.unal.dadm.mova.presentation.viewmodel.places

sealed interface NearbyGymsEvent {
    data class LocationUpdated(val lat: Double, val lng: Double) : NearbyGymsEvent
    data class GymClicked(val gymId: String) : NearbyGymsEvent
    data object BackClicked : NearbyGymsEvent
}
