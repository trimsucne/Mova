package co.edu.unal.dadm.mova.presentation.screen.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import co.edu.unal.dadm.mova.domain.model.Goal

@Composable
fun GoalStep(
    selected: Goal?,
    onSelected: (Goal) -> Unit,
    onContinue: () -> Unit,
    onBack: (() -> Unit)? = null
) {
    AssessmentStep(
        stepIndex = 7,
        totalSteps = 9,
        title = "What's your fitness\ngoal/target?",
        canContinue = selected != null,
        onContinue = onContinue,
        onBack = onBack
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Goal.entries.forEach { goal ->
                val (label, icon) = goal.labelAndIcon()
                OptionButton(
                    label = label,
                    iconText = icon,
                    selected = selected == goal,
                    onClick = { onSelected(goal) }
                )
            }
        }
    }
}

private fun Goal.labelAndIcon(): Pair<String, String> = when (this) {
    Goal.LOSE_WEIGHT -> "I wanna lose weight" to "🏋️"
    Goal.AI_COACH -> "I wanna try AI Coach" to "🤖"
    Goal.GAIN_MUSCLE -> "I wanna get bulks" to "💪"
    Goal.IMPROVE_ENDURANCE -> "I wanna gain endurance" to "⚡"
    Goal.EXPLORE -> "Just trying out the app! 👍" to "📱"
}
