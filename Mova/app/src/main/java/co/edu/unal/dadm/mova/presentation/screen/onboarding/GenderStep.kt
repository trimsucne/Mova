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
import co.edu.unal.dadm.mova.R
import co.edu.unal.dadm.mova.domain.model.Gender
import co.edu.unal.dadm.mova.presentation.components.ImagePlaceholder
import co.edu.unal.dadm.mova.presentation.theme.MovaField
import co.edu.unal.dadm.mova.presentation.theme.MovaInk
import co.edu.unal.dadm.mova.presentation.theme.MovaOrange
import co.edu.unal.dadm.mova.presentation.theme.MovaOrangeSoft

@Composable
fun GenderStep(
    selected: Gender?,
    onSelected: (Gender) -> Unit,
    onContinue: () -> Unit,
    onBack: (() -> Unit)? = null
) {
    AssessmentStep(
        stepIndex = 4,
        totalSteps = 6,
        title = "What is your gender?",
        canContinue = selected != null,
        onContinue = onContinue,
        onBack = onBack
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Male Card
            GenderImageCard(
                symbol = "♂",
                title = "Male",
                label = "Male Runner Photo",
                drawableResId = R.drawable.img_male_runner,
                isSelected = selected == Gender.MALE,
                onClick = { onSelected(Gender.MALE) }
            )

            // Female Card
            GenderImageCard(
                symbol = "♀",
                title = "Female",
                label = "Female Athletic Photo",
                drawableResId = R.drawable.img_female_athlete,
                isSelected = selected == Gender.FEMALE,
                onClick = { onSelected(Gender.FEMALE) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Prefer to skip button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MovaOrangeSoft)
                    .clickable { onSelected(Gender.PREFER_NOT_TO_SAY) }
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Prefer to skip, thanks!",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MovaOrange
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = "✕",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MovaOrange
                    )
                }
            }
        }
    }
}

@Composable
private fun GenderImageCard(
    symbol: String,
    title: String,
    label: String,
    drawableResId: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(MovaField)
            .then(
                if (isSelected) Modifier.border(2.dp, MovaOrange, RoundedCornerShape(22.dp))
                else Modifier
            )
            .clickable(onClick = onClick)
    ) {
        // Gender Photo Image
        ImagePlaceholder(
            label = label,
            drawableResId = drawableResId,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxWidth(0.55f)
                .height(130.dp),
            shape = RoundedCornerShape(topEnd = 22.dp, bottomEnd = 22.dp)
        )

        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = symbol,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MovaInk
                )
                Spacer(modifier = Modifier.size(6.dp))
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MovaInk
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Radio Indicator
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) Color.White else Color.Transparent)
                    .border(
                        width = 2.dp,
                        color = if (isSelected) MovaOrange else Color.Gray.copy(alpha = 0.6f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
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
}
