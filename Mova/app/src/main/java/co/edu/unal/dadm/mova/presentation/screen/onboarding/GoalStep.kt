package co.edu.unal.dadm.mova.presentation.screen.onboarding

import androidx.compose.runtime.Composable
import co.edu.unal.dadm.mova.domain.model.Goal

@Composable
fun GoalStep(selected: Goal?, onSelected: (Goal) -> Unit, onContinue: () -> Unit) {
    AssessmentStep(6, "What's your fitness\ngoal/target?", canContinue = selected != null, onContinue = onContinue) {
        Goal.entries.forEach { goal ->
            OptionButton(goal.label(), selected == goal) { onSelected(goal) }
        }
    }
}

private fun Goal.label() = when (this) {
    Goal.LOSE_WEIGHT -> "▣  I wanna lose weight"
    Goal.AI_COACH -> "▣  I wanna try AI Coach"
    Goal.GAIN_MUSCLE -> "⚒  I wanna get bulks"
    Goal.IMPROVE_ENDURANCE -> "〽  I wanna gain endurance"
    Goal.EXPLORE -> "▣  Just trying out the app! 👍"
}
