package co.edu.unal.dadm.mova.presentation.screen.fitness

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.edu.unal.dadm.mova.presentation.components.TopBarBack
import co.edu.unal.dadm.mova.presentation.theme.MovaOrange
import co.edu.unal.dadm.mova.presentation.theme.MovaTheme

/**
 * Punto de entrada provisional de los modulos Workouts y Actividades hasta que exista la
 * bottom bar / Home (Dev B). Home debe navegar directamente a las [co.edu.unal.dadm.mova.presentation.navigation.FitnessRoutes].
 */
@Composable
fun FitnessHubScreen(
    onWorkouts: () -> Unit,
    onActivities: () -> Unit,
    onHeartRate: () -> Unit,
    onGyms: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        TopBarBack(onBack = onBack, title = "Fitness")
        HubItem("🏋️", "Workouts", "Strength, cardio, yoga and HIIT", onWorkouts)
        HubItem("🏃", "Activities", "Track runs, rides and more with GPS", onActivities)
        HubItem("❤️", "Heart Rate", "Live vitals", onHeartRate)
        HubItem("📍", "Nearby Gyms", "Find a gym and get directions", onGyms)
    }
}

@Composable
private fun HubItem(emoji: String, title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(emoji, fontSize = 32.sp)
        Column(Modifier.weight(1f).padding(start = 16.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(subtitle, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text("›", color = MovaOrange, fontSize = 28.sp, fontWeight = FontWeight.Bold)
    }
}

@Preview(showBackground = true)
@Composable
private fun FitnessHubPreview() {
    MovaTheme(darkTheme = false) { FitnessHubScreen({}, {}, {}, {}, {}) }
}
