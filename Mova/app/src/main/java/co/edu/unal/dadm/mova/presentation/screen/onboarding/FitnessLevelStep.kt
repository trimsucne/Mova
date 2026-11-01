package co.edu.unal.dadm.mova.presentation.screen.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import co.edu.unal.dadm.mova.domain.model.FitnessLevel

@Composable
fun FitnessLevelStep(selected: FitnessLevel?, onSelected: (FitnessLevel) -> Unit, onContinue: () -> Unit) {
    val value = selected ?: FitnessLevel.INTERMEDIATE
    AssessmentStep(3, "How would you rate\nyour fitness level?", "ⓘ Drag to adjust", canContinue = true, onContinue = onContinue) {
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(value.value.toString(), Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center, style = androidx.compose.material3.MaterialTheme.typography.displayLarge)
            Text(value.label(), Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            FitnessLevel.entries.forEach { level ->
                OptionButton(level.value.toString(), value == level) { onSelected(level) }
            }
        }
    }
}

private fun FitnessLevel.label() = name.lowercase().replace('_', ' ').replaceFirstChar { it.uppercase() }
