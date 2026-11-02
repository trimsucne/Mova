package co.edu.unal.dadm.mova.presentation.screen.onboarding

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import co.edu.unal.dadm.mova.presentation.charts.WheelPicker

@Composable
fun AgeStep(
    age: Int?,
    onAgeChanged: (Int) -> Unit,
    onContinue: () -> Unit,
    onBack: (() -> Unit)? = null
) {
    val currentAge = age ?: 19

    AssessmentStep(
        stepIndex = 1,
        totalSteps = 6,
        title = "What's your Age?",
        canContinue = currentAge in 13..120,
        onContinue = onContinue,
        onBack = onBack
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            WheelPicker(
                selectedValue = currentAge,
                onValueChange = onAgeChanged,
                range = 13..100
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
