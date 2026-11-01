package co.edu.unal.dadm.mova.presentation.screen.onboarding

import androidx.compose.runtime.Composable
import co.edu.unal.dadm.mova.domain.model.Diet

@Composable
fun DietStep(selected: Diet?, onSelected: (Diet) -> Unit, onContinue: () -> Unit) {
    AssessmentStep(1, "What's your diet?", "Choose the diet that best fits your eating habits.", selected != null, onContinue) {
        Diet.entries.forEach { diet ->
            OptionButton(diet.label(), selected == diet) { onSelected(diet) }
        }
    }
}

private fun Diet.label() = name.lowercase().replace('_', ' ').replaceFirstChar { it.uppercase() }
