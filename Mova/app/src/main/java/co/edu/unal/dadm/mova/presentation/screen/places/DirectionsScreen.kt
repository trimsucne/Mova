package co.edu.unal.dadm.mova.presentation.screen.places

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import co.edu.unal.dadm.mova.presentation.components.hasFineLocation
import co.edu.unal.dadm.mova.presentation.components.rememberUserLocation
import co.edu.unal.dadm.mova.presentation.theme.MovaInk
import co.edu.unal.dadm.mova.presentation.theme.MovaOrange
import co.edu.unal.dadm.mova.presentation.theme.MovaTheme
import co.edu.unal.dadm.mova.presentation.viewmodel.places.DirectionsEvent
import co.edu.unal.dadm.mova.presentation.viewmodel.places.DirectionsUiState
import co.edu.unal.dadm.mova.presentation.viewmodel.places.DirectionsViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState

@Composable
fun DirectionsRoute(gymId: String, onClose: () -> Unit) {
    val viewModel: DirectionsViewModel = viewModel(
        key = "directions_$gymId",
        factory = viewModelFactory { initializer { DirectionsViewModel(gymId, FitnessContainer.placesRepository, FitnessContainer.directionsRepository) } }
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    val userLocation = rememberUserLocation()
    val context = LocalContext.current
    LaunchedEffect(userLocation.location) {
        userLocation.location?.let { viewModel.onEvent(DirectionsEvent.LocationUpdated(it.lat, it.lng)) }
    }
    DirectionsScreen(state, myLocationEnabled = hasFineLocation(context) && userLocation.permissionGranted == true) { event ->
        when (event) {
            DirectionsEvent.CloseClicked -> onClose()
            DirectionsEvent.OpenExternalMaps -> state.gym?.let { gym ->
                val uri = Uri.parse("google.navigation:q=${gym.lat},${gym.lng}&mode=w")
                val intent = Intent(Intent.ACTION_VIEW, uri)
                runCatching { context.startActivity(intent) }.onFailure {
                    runCatching {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("geo:${gym.lat},${gym.lng}?q=${gym.lat},${gym.lng}(${Uri.encode(gym.name)})")))
                    }
                }
            }
            else -> viewModel.onEvent(event)
        }
    }
}

@Composable
fun DirectionsScreen(state: DirectionsUiState, myLocationEnabled: Boolean, onEvent: (DirectionsEvent) -> Unit) {
    val gym = state.gym
    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        if (gym != null) {
            val dest = LatLng(gym.lat, gym.lng)
            val origin = if (state.userLat != null && state.userLng != null) LatLng(state.userLat, state.userLng) else null
            val camera = rememberCameraPositionState { position = CameraPosition.fromLatLngZoom(dest, 15f) }
            LaunchedEffect(origin) {
                origin?.let {
                    val center = LatLng((it.latitude + dest.latitude) / 2, (it.longitude + dest.longitude) / 2)
                    camera.animate(CameraUpdateFactory.newLatLngZoom(center, 14f))
                }
            }
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = camera,
                properties = MapProperties(isMyLocationEnabled = myLocationEnabled),
                uiSettings = MapUiSettings(zoomControlsEnabled = false, myLocationButtonEnabled = false)
            ) {
                val markerState = rememberMarkerState(key = "gym_${gym.id}", position = dest)
                Marker(
                    state = markerState,
                    title = state.etaMinutes?.let { "Arrival ($it mins)" } ?: gym.name,
                    snippet = gym.name
                )
                if (state.route.size >= 2) {
                    val points = remember(state.route) { state.route.map { LatLng(it.lat, it.lng) } }
                    Polyline(points = points, color = MovaOrange, width = 12f)
                } else if (origin != null) {
                    // Sin ruta por calles (cargando o sin red): linea recta de referencia.
                    Polyline(points = listOf(origin, dest), color = MovaOrange, width = 12f)
                }
            }
        }
        Column(
            modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(shape = RoundedCornerShape(24.dp), shadowElevation = 8.dp, color = MaterialTheme.colorScheme.surface) {
                Column(Modifier.fillMaxWidth().padding(20.dp)) {
                    Text(state.instruction ?: "Locating you…", color = MovaOrange, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(gym?.name ?: if (state.loading) "Loading…" else "Gym not found", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                    Text(
                        state.distanceMeters?.let { "${GeoUtils.formatDistance(it)} Left" } ?: (gym?.address ?: ""),
                        color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.weight(1f).height(54.dp).clickable { onEvent(DirectionsEvent.OpenExternalMaps) },
                    shape = RoundedCornerShape(16.dp), color = MovaInk
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("Open in Maps", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
                Box(
                    modifier = Modifier
                        .size(54.dp).clip(RoundedCornerShape(16.dp)).background(MovaOrange)
                        .clickable { onEvent(DirectionsEvent.CloseClicked) },
                    contentAlignment = Alignment.Center
                ) { Text("✕", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold) }
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 400)
@Composable
private fun DirectionsPreview() {
    MovaTheme(darkTheme = false) {
        DirectionsScreen(
            DirectionsUiState(
                loading = false, gym = Gym("1", "Campus Fitness Center", "Ciudad Universitaria", 4.64, -74.08),
                distanceMeters = 850.0, etaMinutes = 11, instruction = "Head north-east"
            ), false, {}
        )
    }
}
