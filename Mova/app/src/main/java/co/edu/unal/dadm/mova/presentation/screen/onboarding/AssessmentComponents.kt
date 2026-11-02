package co.edu.unal.dadm.mova.presentation.screen.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import co.edu.unal.dadm.mova.presentation.components.MovaPrimaryButton
import co.edu.unal.dadm.mova.presentation.components.StepHeader
import co.edu.unal.dadm.mova.presentation.theme.MovaField
import co.edu.unal.dadm.mova.presentation.theme.MovaInk
import co.edu.unal.dadm.mova.presentation.theme.MovaMuted
import co.edu.unal.dadm.mova.presentation.theme.MovaOrange

@Composable
internal fun AssessmentStep(
    stepIndex: Int,
    totalSteps: Int = 6,
    title: String,
    description: String = "",
    canContinue: Boolean = true,
    onContinue: () -> Unit,
    onBack: (() -> Unit)? = null,
    accent: Boolean = false,
    headerTitle: String = "Assessment",
    content: @Composable () -> Unit
) {
    val backgroundColor = if (accent) MovaOrange else Color.White
    val foregroundColor = if (accent) Color.White else MovaInk

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        StepHeader(
            currentStep = stepIndex,
            totalSteps = totalSteps,
            title = headerTitle,
            onBack = onBack,
            foreground = foregroundColor
        )

        Text(
            text = title,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = foregroundColor,
            lineHeight = 34.sp
        )

        if (description.isNotBlank()) {
            Text(
                text = description,
                fontSize = 14.sp,
                color = if (accent) Color.White.copy(alpha = 0.85f) else MovaMuted,
                lineHeight = 20.sp
            )
        }

        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            content()
        }

        MovaPrimaryButton(
            text = "Continue",
            onClick = onContinue,
            enabled = canContinue,
            containerColor = if (accent) Color.White else MovaInk,
            contentColor = if (accent) MovaOrange else Color.White
        )
    }
}

@Composable
internal fun OptionButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconText: String? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) MovaOrange else MovaField)
            .then(
                if (selected) Modifier.border(2.dp, MovaOrange, RoundedCornerShape(16.dp))
                else Modifier
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!iconText.isNullOrEmpty()) {
            Text(
                text = iconText,
                fontSize = 20.sp,
                modifier = Modifier.padding(end = 12.dp)
            )
        }

        Text(
            text = label,
            fontSize = 15.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) Color.White else MovaInk,
            modifier = Modifier.weight(1f)
        )

        // Radio button circle
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(if (selected) Color.White else Color.Transparent)
                .border(
                    width = 2.dp,
                    color = if (selected) Color.White else Color.Gray.copy(alpha = 0.6f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (selected) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(MovaOrange)
                )
            }
        }
    }
}
