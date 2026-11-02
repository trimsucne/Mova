package co.edu.unal.dadm.mova.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.edu.unal.dadm.mova.presentation.theme.MovaField
import co.edu.unal.dadm.mova.presentation.theme.MovaInk
import co.edu.unal.dadm.mova.presentation.theme.MovaStepBadgeBg
import co.edu.unal.dadm.mova.presentation.theme.MovaStepBadgeText

@Composable
fun StepHeader(
    currentStep: Int,
    totalSteps: Int = 6,
    title: String = "Assessment",
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    foreground: Color = MovaInk
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBack != null) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (foreground == Color.White) Color.White.copy(alpha = 0.2f) else MovaField)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "‹",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = foreground
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
        }

        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = foreground
        )

        Spacer(modifier = Modifier.weight(1f))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(if (foreground == Color.White) Color.White.copy(alpha = 0.25f) else MovaStepBadgeBg)
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = "$currentStep of $totalSteps",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (foreground == Color.White) Color.White else MovaStepBadgeText
            )
        }
    }
}
