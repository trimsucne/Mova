package co.edu.unal.dadm.mova.presentation.screen.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import co.edu.unal.dadm.mova.domain.model.Diet

@Composable
fun DietStep(
    selected: Diet?,
    onSelected: (Diet) -> Unit,
    onContinue: () -> Unit,
    onBack: (() -> Unit)? = null
) {
    AssessmentStep(
        stepIndex = 1,
        totalSteps = 6,
        headerTitle = "Dietary options",
        title = "What's your diet?",
        description = "Choose the diet that best fits your eating habits.",
        canContinue = selected != null,
        onContinue = onContinue,
        onBack = onBack
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Diet.entries.forEach { diet ->
                OptionButton(
                    label = diet.label(),
                    selected = selected == diet,
                    onClick = { onSelected(diet) }
                )
            }
        }
    }
}

private fun Diet.label() = name.lowercase().replace('_', ' ').replaceFirstChar { it.uppercase() }
