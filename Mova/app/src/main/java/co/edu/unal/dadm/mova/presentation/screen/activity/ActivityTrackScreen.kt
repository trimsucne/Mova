package co.edu.unal.dadm.mova.presentation.screen.activity

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import co.edu.unal.dadm.mova.domain.model.ActivityType
import co.edu.unal.dadm.mova.domain.model.RoutePoint
import co.edu.unal.dadm.mova.domain.model.TrackingState
import co.edu.unal.dadm.mova.domain.model.TrackingStatus
import co.edu.unal.dadm.mova.presentation.components.StatColumn
import co.edu.unal.dadm.mova.presentation.components.TopBarBack
import co.edu.unal.dadm.mova.presentation.components.formatClock
import co.edu.unal.dadm.mova.presentation.components.hasFineLocation
import co.edu.unal.dadm.mova.presentation.theme.MovaInk
import co.edu.unal.dadm.mova.presentation.theme.MovaOrange
import co.edu.unal.dadm.mova.presentation.theme.MovaTheme
import co.edu.unal.dadm.mova.presentation.viewmodel.activity.ActivityTrackEvent
import co.edu.unal.dadm.mova.presentation.viewmodel.activity.ActivityTrackUiState
import co.edu.unal.dadm.mova.presentation.viewmodel.activity.ActivityTrackViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState

private val DefaultCenter = LatLng(4.6382, -74.0840)

@Composable
fun ActivityTrackRoute(type: ActivityType, onSaved: (logId: String) -> Unit, onExit: () -> Unit) {
    val viewModel: ActivityTrackViewModel = viewModel(
        key = "track_${type.id}",
        factory = viewModelFactory {
            initializer { ActivityTrackViewModel(type, FitnessContainer.trackingRepository, FitnessContainer.logActivity) }
        }
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var fineGranted by remember { mutableStateOf(hasFineLocation(context)) }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        fineGranted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true
        // Sin GPS no hay ruta, pero las actividades sin GPS siempre pueden iniciar.
        if (fineGranted || !type.usesGps) viewModel.onEvent(ActivityTrackEvent.StartRequested)
        else viewModel.onEvent(ActivityTrackEvent.PermissionDenied)
    }

    LaunchedEffect(state.savedLogId) { state.savedLogId?.let(onSaved) }
    LaunchedEffect(state.discarded) { if (state.discarded) onExit() }
    // Mientras hay sesion activa, atras no sale (evita perder la sesion por accidente).
    BackHandler(enabled = state.running) {}

    ActivityTrackScreen(
        state = state,
        fineGranted = fineGranted,
        onOpenSettings = {
            context.startActivity(
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
            )
        }
    ) { event ->
        when (event) {
            ActivityTrackEvent.StartRequested -> {
                val perms = buildList {
                    if (type.usesGps) {
                        add(Manifest.permission.ACCESS_FINE_LOCATION)
                        add(Manifest.permission.ACCESS_COARSE_LOCATION)
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) add(Manifest.permission.POST_NOTIFICATIONS)
                }
                if (perms.isEmpty()) viewModel.onEvent(event) else permissionLauncher.launch(perms.toTypedArray())
            }
            ActivityTrackEvent.BackClicked -> onExit()
            else -> viewModel.onEvent(event)
        }
    }
}

@Composable
fun ActivityTrackScreen(
    state: ActivityTrackUiState,
    fineGranted: Boolean,
    onOpenSettings: () -> Unit,
    onEvent: (ActivityTrackEvent) -> Unit
) {
    val type = state.type
    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        if (type.usesGps) {
            TrackingMap(route = state.tracking.route, fineGranted = fineGranted)
        } else {
            Box(Modifier.fillMaxSize(), Alignment.Center) { Text(type.emoji(), fontSize = 96.sp) }
        }
        TopBarBack(
            onBack = { onEvent(ActivityTrackEvent.BackClicked) },
            modifier = Modifier.align(Alignment.TopStart).statusBarsPadding().padding(horizontal = 20.dp)
        )
        Surface(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().navigationBarsPadding(),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            color = MovaInk,
            shadowElevation = 12.dp
        ) {
            Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("${type.emoji()} ${type.label}", color = Color.White.copy(alpha = 0.8f), fontWeight = FontWeight.Bold)
                Text(
                    formatClock(state.tracking.elapsedSec), color = Color.White,
                    fontSize = 52.sp, fontWeight = FontWeight.ExtraBold
                )
                if (type.usesGps) {
                    Row(Modifier.fillMaxWidth().padding(vertical = 16.dp)) {
                        StatColumn(GeoUtils.formatDistance(state.tracking.distanceMeters), "Distance", Modifier.weight(1f))
                        StatColumn(state.tracking.paceMinPerKm?.let { formatPace(it) } ?: "--", "Pace /km", Modifier.weight(1f))
                    }
                }
                if (state.permissionDenied) {
                    Text(
                        "Location permission is needed to draw your route.",
                        color = MaterialTheme.colorScheme.error, fontSize = 13.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedButton(onClick = onOpenSettings, modifier = Modifier.padding(bottom = 8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)) { Text("Open settings") }
                }
                when (state.tracking.status) {
                    TrackingStatus.Idle -> Button(
                        onClick = { onEvent(ActivityTrackEvent.StartRequested) },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MovaOrange, contentColor = Color.White)
                    ) { Text("Start", fontWeight = FontWeight.Bold, fontSize = 17.sp, modifier = Modifier.padding(vertical = 8.dp)) }
                    else -> Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            onClick = { onEvent(ActivityTrackEvent.PauseResumeClicked) },
                            modifier = Modifier.weight(1f), shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                        ) {
                            Text(
                                if (state.tracking.status == TrackingStatus.Paused) "Resume" else "Pause",
                                fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        Button(
                            onClick = { onEvent(ActivityTrackEvent.FinishClicked) },
                            enabled = !state.saving,
                            modifier = Modifier.weight(1f), shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MovaOrange, contentColor = Color.White)
                        ) { Text("Finish", fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun TrackingMap(route: List<RoutePoint>, fineGranted: Boolean) {
    val camera = rememberCameraPositionState { position = CameraPosition.fromLatLngZoom(DefaultCenter, 15f) }
    val points = remember(route) { route.map { LatLng(it.lat, it.lng) } }
    LaunchedEffect(points.lastOrNull()) {
        points.lastOrNull()?.let { camera.animate(CameraUpdateFactory.newLatLngZoom(it, 17f)) }
    }
    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = camera,
        properties = MapProperties(isMyLocationEnabled = fineGranted),
        uiSettings = MapUiSettings(zoomControlsEnabled = false, myLocationButtonEnabled = fineGranted)
    ) {
        if (points.size >= 2) Polyline(points = points, color = MovaOrange, width = 14f)
    }
}

private fun formatPace(minPerKm: Double): String {
    val totalSec = (minPerKm * 60).toInt()
    return String.format("%d:%02d", totalSec / 60, totalSec % 60)
}

@Preview(showBackground = true, heightDp = 700)
@Composable
private fun ActivityTrackIdlePreview() {
    MovaTheme(darkTheme = false) {
        ActivityTrackScreen(ActivityTrackUiState(ActivityType.Weightlifting), false, {}, {})
    }
}

@Preview(showBackground = true, heightDp = 700)
@Composable
private fun ActivityTrackRunningPreview() {
    MovaTheme(darkTheme = true) {
        ActivityTrackScreen(
            ActivityTrackUiState(
                ActivityType.Yoga,
                tracking = TrackingState(TrackingStatus.Running, ActivityType.Yoga, 0, 754)
            ),
            false, {}, {}
        )
    }
}
