package co.edu.unal.dadm.mova.presentation.screen.onboarding

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun WeightStep(weightKg: Double?, onWeightChanged: (Double) -> Unit, onContinue: () -> Unit) {
    val value = weightKg?.toString().orEmpty()
    AssessmentStep(2, "What's your current weight?", "Choose kilograms for your profile.", weightKg?.let { it in 25.0..400.0 } == true, onContinue) {
        OutlinedTextField(
            value = value,
            onValueChange = { it.toDoubleOrNull()?.let(onWeightChanged) },
            label = { Text("Weight (kg)") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
