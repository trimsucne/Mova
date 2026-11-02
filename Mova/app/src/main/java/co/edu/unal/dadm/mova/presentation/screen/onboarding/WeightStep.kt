package co.edu.unal.dadm.mova.presentation.screen.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.edu.unal.dadm.mova.presentation.charts.RulerPicker
import co.edu.unal.dadm.mova.presentation.theme.MovaOrange

@Composable
fun WeightStep(
    weightKg: Double?,
    onWeightChanged: (Double) -> Unit,
    onContinue: () -> Unit,
    onBack: (() -> Unit)? = null
) {
    val weight = (weightKg ?: 62.0).toFloat()
    var isKg by remember { mutableStateOf(true) }

    AssessmentStep(
        stepIndex = 2,
        totalSteps = 6,
        title = "What's your current weight right now?",
        canContinue = weight in 25.0f..300.0f,
        onContinue = onContinue,
        onBack = onBack,
        accent = true
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Kg / Lbs unit toggle pills
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .height(44.dp)
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isKg) Color.White else Color.White.copy(alpha = 0.3f))
                        .clickable { isKg = true },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Kg",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isKg) MovaOrange else Color.White
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Box(
                    modifier = Modifier
                        .height(44.dp)
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (!isKg) Color.White else Color.White.copy(alpha = 0.3f))
                        .clickable { isKg = false },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Lbs",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (!isKg) MovaOrange else Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            val displayWeight = if (isKg) weight else (weight * 2.20462f)
            val unitLabel = if (isKg) "Kg" else "Lbs"

            Text(
                text = "${displayWeight.toInt()} $unitLabel",
                fontSize = 44.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            RulerPicker(
                value = weight,
                onValueChange = { onWeightChanged(it.toDouble()) },
                range = 30f..160f,
                lineColor = Color.White,
                centerIndicatorColor = Color.White
            )
        }
    }
}
