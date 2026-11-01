package co.edu.unal.dadm.mova.presentation.screen.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import co.edu.unal.dadm.mova.ui.theme.MovaOrange

@Composable
fun AgeStep(age: Int?, onAgeChanged: (Int) -> Unit, onContinue: () -> Unit) {
    val current = age ?: 19
    AssessmentStep(1, "What's your age?", canContinue = current in 13..120, onContinue = onContinue) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            listOf(current - 2, current - 1, current, current + 1, current + 2).forEach { value ->
                Text(
                    value.toString(),
                    color = if (value == current) MovaOrange else androidx.compose.ui.graphics.Color.Gray,
                    style = if (value == current) androidx.compose.material3.MaterialTheme.typography.displaySmall
                    else androidx.compose.material3.MaterialTheme.typography.headlineSmall
                )
            }
            OptionButton("Selected age: $current", true) { onAgeChanged(current) }
        }
    }
}
