package co.edu.unal.dadm.mova.presentation.screen.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun VoiceStep(onContinue: () -> Unit) {
    AssessmentStep(5, "AI Vocal Analysis", "Your voice is connected to your health. Say the following for better assessment. 🙌", true, onContinue) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            repeat(13) { Text("▮", color = if (it in 5..7) Color.DarkGray else Color.LightGray) }
        }
        Text("If there’s no pain, then\nthere’s always no gain.", style = androidx.compose.material3.MaterialTheme.typography.headlineSmall)
    }
}
