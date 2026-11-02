package co.edu.unal.dadm.mova.presentation.screen.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.edu.unal.dadm.mova.domain.model.FitnessLevel
import co.edu.unal.dadm.mova.presentation.charts.ArcSlider
import co.edu.unal.dadm.mova.presentation.theme.MovaField
import co.edu.unal.dadm.mova.presentation.theme.MovaInk
import co.edu.unal.dadm.mova.presentation.theme.MovaMuted
import co.edu.unal.dadm.mova.presentation.theme.MovaOrange

@Composable
fun FitnessLevelStep(
    selected: FitnessLevel?,
    onSelected: (FitnessLevel) -> Unit,
    onContinue: () -> Unit,
    onBack: (() -> Unit)? = null
) {
    val currentLevel = selected ?: FitnessLevel.INTERMEDIATE
    val currentLevelValue = currentLevel.value.coerceIn(1, 5)

    AssessmentStep(
        stepIndex = 4,
        totalSteps = 9,
        title = "How would you rate\nyour fitness level?",
        canContinue = true,
        onContinue = onContinue,
        onBack = onBack
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "💡",
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.size(6.dp))
                Text(
                    text = "Drag arc or tap level below",
                    fontSize = 13.sp,
                    color = MovaMuted,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Centered Symmetric Arc Gauge Slider
            ArcSlider(
                value = currentLevelValue,
                onValueChange = { levelVal ->
                    val matchedLevel = FitnessLevel.entries.find { it.value == levelVal } ?: FitnessLevel.INTERMEDIATE
                    onSelected(matchedLevel)
                },
                range = 1..5
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Selected Level Number and Description
            Text(
                text = currentLevelValue.toString(),
                fontSize = 52.sp,
                fontWeight = FontWeight.Bold,
                color = MovaInk,
                textAlign = TextAlign.Center
            )

            Text(
                text = currentLevel.label(),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MovaOrange,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Level Selector Chips (1, 2, 3, 4, 5)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FitnessLevel.entries.forEach { level ->
                    val isSelected = level == currentLevel
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) MovaOrange else MovaField)
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) MovaOrange else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable { onSelected(level) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = level.value.toString(),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else MovaInk
                        )
                    }
                }
            }
        }
    }
}

private fun FitnessLevel.label() = when (this) {
    FitnessLevel.BEGINNER -> "Beginner"
    FitnessLevel.NOVICE -> "Novice"
    FitnessLevel.INTERMEDIATE -> "Somewhat Athletic"
    FitnessLevel.ADVANCED -> "Athletic"
    FitnessLevel.ATHLETE -> "Expert Athlete"
}
