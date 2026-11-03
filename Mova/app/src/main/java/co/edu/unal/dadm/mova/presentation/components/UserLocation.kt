package co.edu.unal.dadm.mova.presentation.components

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource

data class LatLngPoint(val lat: Double, val lng: Double)

data class UserLocationState(
    /** null = aun no se sabe; false = denegado. */
    val permissionGranted: Boolean?,
    val location: LatLngPoint?
)

fun hasFineLocation(context: android.content.Context): Boolean =
    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

/**
 * Pide permiso de ubicacion (si falta) y obtiene una posicion actual puntual.
 * Si se deniega, [UserLocationState.permissionGranted] sera false y la pantalla debe degradar.
 */
@SuppressLint("MissingPermission") // se verifica con hasFineLocation antes de usar
@Composable
fun rememberUserLocation(): UserLocationState {
    val context = LocalContext.current
    var granted by remember { mutableStateOf<Boolean?>(if (hasFineLocation(context)) true else null) }
    var location by remember { mutableStateOf<LatLngPoint?>(null) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        granted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }
    LaunchedEffect(Unit) {
        if (granted != true) {
            launcher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        }
    }
    LaunchedEffect(granted) {
        if (granted == true) {
            val client = LocationServices.getFusedLocationProviderClient(context)
            client.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, CancellationTokenSource().token)
                .addOnSuccessListener { loc -> if (loc != null) location = LatLngPoint(loc.latitude, loc.longitude) }
        }
    }
    return UserLocationState(granted, location)
}
