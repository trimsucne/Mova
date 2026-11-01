package co.edu.unal.dadm.mova.presentation.screen.onboarding

import androidx.compose.runtime.Composable
import co.edu.unal.dadm.mova.domain.model.Gender

@Composable
fun GenderStep(selected: Gender?, onSelected: (Gender) -> Unit, onContinue: () -> Unit) {
    AssessmentStep(4, "What is your gender?", "Used only to personalize recommendations.", selected != null, onContinue) {
        Gender.entries.forEach { gender ->
            OptionButton(gender.label(), selected == gender) { onSelected(gender) }
        }
    }
}

private fun Gender.label() = name.lowercase().replace('_', ' ').replaceFirstChar { it.uppercase() }
