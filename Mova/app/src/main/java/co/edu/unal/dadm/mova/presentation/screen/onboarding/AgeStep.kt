package co.edu.unal.dadm.mova.presentation.screen.onboarding

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun AgeStep(age: Int?, onAgeChanged: (Int) -> Unit, onContinue: () -> Unit) {
    val value = age?.toString().orEmpty()
    AssessmentStep(0, "What's your age?", "Your voice is connected to your health.", value.toIntOrNull() in 13..120, onContinue) {
        OutlinedTextField(
            value = value,
            onValueChange = { it.toIntOrNull()?.let(onAgeChanged) },
            label = { Text("Age") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
