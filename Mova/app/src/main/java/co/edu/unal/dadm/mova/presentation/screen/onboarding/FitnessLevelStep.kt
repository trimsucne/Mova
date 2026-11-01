package co.edu.unal.dadm.mova.presentation.screen.onboarding

import androidx.compose.runtime.Composable
import co.edu.unal.dadm.mova.domain.model.FitnessLevel

@Composable
fun FitnessLevelStep(selected: FitnessLevel?, onSelected: (FitnessLevel) -> Unit, onContinue: () -> Unit) {
    AssessmentStep(3, "How would you rate your fitness level?", "Drag-free MVP selector.", selected != null, onContinue) {
        FitnessLevel.entries.forEach { level ->
            OptionButton("${level.value} - ${level.label()}", selected == level) { onSelected(level) }
        }
    }
}

private fun FitnessLevel.label() = name.lowercase().replace('_', ' ').replaceFirstChar { it.uppercase() }
