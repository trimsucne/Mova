package co.edu.unal.dadm.mova.presentation.screen.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import co.edu.unal.dadm.mova.ui.theme.MovaOrange

@Composable
fun WeightStep(weightKg: Double?, onWeightChanged: (Double) -> Unit, onContinue: () -> Unit) {
    val weight = weightKg ?: 62.0
    AssessmentStep(2, "What's your current\nweight right now?", canContinue = weight in 25.0..400.0, onContinue = onContinue, accent = true) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            OptionButton("Kg", true) {}
            OptionButton("Lbs", false) {}
        }
        Text("${weight.toInt()} Kg", Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center, color = Color.White, style = androidx.compose.material3.MaterialTheme.typography.displaySmall)
        Text("50   55   60   65   70   75   80", Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center, color = Color.White)
        OptionButton("Adjust weight: ${weight.toInt()} kg", true) { onWeightChanged(weight) }
    }
}
