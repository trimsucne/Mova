package co.edu.unal.dadm.mova.presentation.screen.activity

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.health.connect.client.PermissionController
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import co.edu.unal.dadm.mova.R
import co.edu.unal.dadm.mova.core.di.FitnessContainer
import co.edu.unal.dadm.mova.core.health.HealthPermissions
import co.edu.unal.dadm.mova.presentation.theme.MovaOrange
import co.edu.unal.dadm.mova.domain.model.VitalSample
import co.edu.unal.dadm.mova.presentation.components.TopBarBack
import co.edu.unal.dadm.mova.presentation.theme.MovaTheme
import co.edu.unal.dadm.mova.presentation.viewmodel.activity.HeartRateEvent
import co.edu.unal.dadm.mova.presentation.viewmodel.activity.HeartRateUiState
import co.edu.unal.dadm.mova.presentation.viewmodel.activity.HeartRateViewModel

@Composable
fun HeartRateRoute(onBack: () -> Unit) {
    val context = LocalContext.current
    var refresh by remember { mutableIntStateOf(0) }
    var needsConnect by remember { mutableStateOf(false) }
    LaunchedEffect(refresh) { needsConnect = HealthPermissions.needsPermission(context) }
    val permissionLauncher = rememberLauncherForActivityResult(
        PermissionController.createRequestPermissionResultContract()
    ) { refresh++ }

    // La key fuerza un ViewModel nuevo (y una nueva suscripcion) al cambiar los permisos.
    val viewModel: HeartRateViewModel = viewModel(
        key = "heart_rate_$refresh",
        factory = viewModelFactory {
            initializer { HeartRateViewModel(FitnessContainer.healthDataSource, FitnessContainer.trackingRepository) }
        }
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    HeartRateScreen(
        state = state,
        showConnect = needsConnect,
        onConnect = { permissionLauncher.launch(HealthPermissions.required) }
    ) { if (it == HeartRateEvent.BackClicked) onBack() }
}

@Composable
fun HeartRateScreen(
    state: HeartRateUiState,
    showConnect: Boolean = false,
    onConnect: () -> Unit = {},
    onEvent: (HeartRateEvent) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().statusBarsPadding().padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                TopBarBack(onBack = { onEvent(HeartRateEvent.BackClicked) }, modifier = Modifier.align(Alignment.CenterStart))
                Text("Heart Rate", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
            }
            if (showConnect) {
                Button(
                    onClick = onConnect,
                    modifier = Modifier.padding(top = 16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MovaOrange, contentColor = Color.White)
                ) { Text("Connect Health Connect", fontWeight = FontWeight.Bold) }
            }
            Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.padding(top = 32.dp)) {
                Text(state.sample?.bpm?.toString() ?: "--", fontSize = 64.sp, fontWeight = FontWeight.ExtraBold)
                Text("bpm", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 10.dp))
            }
            Text(
                "Currently doing: ${state.currentActivity}", fontSize = 16.sp, fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 8.dp)
            )
            Image(
                painter = painterResource(R.drawable.img_male_runner),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                colorFilter = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) }),
                modifier = Modifier.weight(1f).fillMaxWidth().padding(vertical = 16.dp)
            )
        }
        Row(
            modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(16.dp),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(14.dp)
        ) {
            VitalCard("Pressure", state.sample?.systolic?.toString() ?: "--", "mmHg", Modifier.weight(1f))
            VitalCard("Oxygen", state.sample?.spo2?.toString() ?: "--", "SpO2", Modifier.weight(1f))
        }
    }
}

@Composable
private fun VitalCard(title: String, value: String, unit: String, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(24.dp), shadowElevation = 6.dp, color = MaterialTheme.colorScheme.surface) {
        Column(Modifier.background(MaterialTheme.colorScheme.surface).padding(18.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.padding(top = 10.dp)) {
                Text(value, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
                Text(unit, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 6.dp, bottom = 4.dp))
            }
        }
    }
}

private val sample = HeartRateUiState(VitalSample(112, 112, 98, 0), "Basketball")

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun HeartRatePreview() {
    MovaTheme(darkTheme = false) { HeartRateScreen(sample, onEvent = {}) }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun HeartRateDarkPreview() {
    MovaTheme(darkTheme = true) { HeartRateScreen(sample, onEvent = {}) }
}
