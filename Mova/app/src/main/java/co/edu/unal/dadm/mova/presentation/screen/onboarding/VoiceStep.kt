package co.edu.unal.dadm.mova.presentation.screen.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import co.edu.unal.dadm.mova.presentation.charts.WaveformVisualizer
import co.edu.unal.dadm.mova.presentation.theme.MovaInk
import co.edu.unal.dadm.mova.presentation.theme.MovaOrange

@Composable
fun VoiceStep(
    onContinue: () -> Unit,
    onBack: (() -> Unit)? = null
) {
    AssessmentStep(
        stepIndex = 6,
        totalSteps = 9,
        title = "AI Vocal Analysis",
        description = "Your voice is connected to your health. Say the following for better assessment. 🙌",
        canContinue = true,
        onContinue = onContinue,
        onBack = onBack
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Waveform animation
            WaveformVisualizer()

            Spacer(modifier = Modifier.height(28.dp))

            // Phrase to speak with active words highlighted in orange pill
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MovaOrange)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "If there's",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = " no pain, then",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MovaInk
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "there's always no gain.",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MovaInk
                )
            }
        }
    }
}
