package co.edu.unal.dadm.mova.presentation.screen.onboarding

import androidx.compose.runtime.Composable
import co.edu.unal.dadm.mova.domain.model.Goal

@Composable
fun GoalStep(selected: Goal?, onSelected: (Goal) -> Unit, onContinue: () -> Unit) {
    AssessmentStep(5, "What's your fitness goal?", "Choose one target to start.", selected != null, onContinue) {
        Goal.entries.forEach { goal ->
            OptionButton(goal.label(), selected == goal) { onSelected(goal) }
        }
    }
}

private fun Goal.label() = name.lowercase().replace('_', ' ').replaceFirstChar { it.uppercase() }
