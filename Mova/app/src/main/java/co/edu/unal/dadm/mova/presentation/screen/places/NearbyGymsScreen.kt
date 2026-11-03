package co.edu.unal.dadm.mova.presentation.screen.places

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import co.edu.unal.dadm.mova.core.common.GeoUtils
import co.edu.unal.dadm.mova.core.di.FitnessContainer
import co.edu.unal.dadm.mova.domain.model.Gym
import co.edu.unal.dadm.mova.presentation.components.TopBarBack
import co.edu.unal.dadm.mova.presentation.components.rememberUserLocation
import co.edu.unal.dadm.mova.presentation.theme.MovaOrange
import co.edu.unal.dadm.mova.presentation.theme.MovaTheme
import co.edu.unal.dadm.mova.presentation.viewmodel.places.GymWithDistance
import co.edu.unal.dadm.mova.presentation.viewmodel.places.NearbyGymsEvent
import co.edu.unal.dadm.mova.presentation.viewmodel.places.NearbyGymsUiState
import co.edu.unal.dadm.mova.presentation.viewmodel.places.NearbyGymsViewModel

@Composable
fun NearbyGymsRoute(onGymClick: (String) -> Unit, onBack: () -> Unit) {
    val viewModel: NearbyGymsViewModel = viewModel(
        factory = viewModelFactory { initializer { NearbyGymsViewModel(FitnessContainer.placesRepository) } }
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    val userLocation = rememberUserLocation()
    LaunchedEffect(userLocation.location) {
        userLocation.location?.let { viewModel.onEvent(NearbyGymsEvent.LocationUpdated(it.lat, it.lng)) }
    }
    NearbyGymsScreen(state, locationDenied = userLocation.permissionGranted == false) { event ->
        when (event) {
            is NearbyGymsEvent.GymClicked -> onGymClick(event.gymId)
            NearbyGymsEvent.BackClicked -> onBack()
            else -> viewModel.onEvent(event)
        }
    }
}

@Composable
fun NearbyGymsScreen(state: NearbyGymsUiState, locationDenied: Boolean, onEvent: (NearbyGymsEvent) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(horizontal = 20.dp, vertical = 8.dp)) {
        TopBarBack(onBack = { onEvent(NearbyGymsEvent.BackClicked) }, title = "Nearby Gyms")
        if (locationDenied) {
            Text(
                "Location is off: showing gyms without distance.", fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        if (state.loading) {
            Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator(color = MovaOrange) }
            return@Column
        }
        LazyColumn(modifier = Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(state.gyms, key = { it.gym.id }) { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { onEvent(NearbyGymsEvent.GymClicked(item.gym.id)) }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(Modifier.size(52.dp).clip(RoundedCornerShape(16.dp)).background(MovaOrange), contentAlignment = Alignment.Center) {
                        Text("🏋️", fontSize = 26.sp)
                    }
                    Column(Modifier.weight(1f).padding(horizontal = 14.dp)) {
                        Text(item.gym.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(item.gym.address, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(
                        item.distanceMeters?.let { GeoUtils.formatDistance(it) } ?: "",
                        color = MovaOrange, fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private val sample = NearbyGymsUiState(
    loading = false,
    gyms = listOf(
        GymWithDistance(Gym("1", "Campus Fitness Center", "Ciudad Universitaria", 0.0, 0.0), 850.0),
        GymWithDistance(Gym("2", "Teusaquillo Gym", "Cra. 24 #39", 0.0, 0.0), 2400.0)
    )
)

@Preview(showBackground = true, heightDp = 600)
@Composable
private fun NearbyGymsPreview() {
    MovaTheme(darkTheme = false) { NearbyGymsScreen(sample, false, {}) }
}

@Preview(showBackground = true, heightDp = 600)
@Composable
private fun NearbyGymsDarkPreview() {
    MovaTheme(darkTheme = true) { NearbyGymsScreen(sample, true, {}) }
}
