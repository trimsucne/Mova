package co.edu.unal.dadm.mova.presentation.screen.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
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
import kotlin.math.roundToInt

@Composable
fun HeightStep(
    heightCm: Int?,
    onHeightChanged: (Int) -> Unit,
    onContinue: () -> Unit,
    onBack: (() -> Unit)? = null
) {
    val height = (heightCm ?: 170).toFloat()
    var isCm by remember { mutableStateOf(true) }

    AssessmentStep(
        stepIndex = 3,
        totalSteps = 9,
        title = "What's your height?",
        canContinue = height in 100f..250f,
        onContinue = onContinue,
        onBack = onBack,
        accent = true
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                UnitPill("Cm", selected = isCm) { isCm = true }
                Spacer(modifier = Modifier.width(16.dp))
                UnitPill("Ft", selected = !isCm) { isCm = false }
            }

            Spacer(modifier = Modifier.height(36.dp))

            Text(
                text = if (isCm) "${height.roundToInt()} cm" else feetAndInches(height),
                fontSize = 44.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            RulerPicker(
                value = height,
                onValueChange = { onHeightChanged(it.roundToInt()) },
                range = 100f..250f,
                lineColor = Color.White,
                centerIndicatorColor = Color.White
            )
        }
    }
}

@Composable
private fun RowScope.UnitPill(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .height(44.dp)
            .weight(1f)
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) Color.White else Color.White.copy(alpha = 0.3f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = if (selected) MovaOrange else Color.White
        )
    }
}

private fun feetAndInches(cm: Float): String {
    val totalInches = (cm / 2.54f).roundToInt()
    return "${totalInches / 12} ft ${totalInches % 12} in"
}
