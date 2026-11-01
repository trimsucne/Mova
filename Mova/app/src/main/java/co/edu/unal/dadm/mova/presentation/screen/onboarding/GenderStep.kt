package co.edu.unal.dadm.mova.presentation.screen.onboarding

import androidx.compose.runtime.Composable
import co.edu.unal.dadm.mova.domain.model.Gender

@Composable
fun GenderStep(selected: Gender?, onSelected: (Gender) -> Unit, onContinue: () -> Unit) {
    AssessmentStep(4, "What is your gender?", canContinue = selected != null, onContinue = onContinue) {
        Gender.entries.filter { it != Gender.PREFER_NOT_TO_SAY }.forEach { gender ->
            OptionButton(if (gender == Gender.MALE) "♂  Male" else "♀  Female", selected == gender) { onSelected(gender) }
        }
        OptionButton("Prefer to skip, thanks  ×", selected == Gender.PREFER_NOT_TO_SAY) {
            onSelected(Gender.PREFER_NOT_TO_SAY)
        }
    }
}
