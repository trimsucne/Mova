package co.edu.unal.dadm.mova.presentation.charts

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import co.edu.unal.dadm.mova.presentation.theme.MovaInk

@Composable
fun WaveformVisualizer(
    modifier: Modifier = Modifier,
    barCount: Int = 13,
    activeColor: Color = MovaInk,
    inactiveColor: Color = Color(0xFFDCDDE1)
) {
    val transition = rememberInfiniteTransition(label = "WaveformAnim")
    val animScale by transition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "waveformScale"
    )

    // Default bar heights pattern matching mockup
    val baseHeights = listOf(
        0.25f, 0.55f, 0.45f, 0.65f, 0.50f, 0.85f, 1.0f,
        0.70f, 0.80f, 0.55f, 0.95f, 0.60f, 0.35f
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxWidth().height(200.dp)) {
            val width = size.width
            val height = size.height
            val centerY = height / 2f

            val barWidth = 14f
            val spacing = (width - (barCount * barWidth)) / (barCount + 1)

            for (i in 0 until barCount) {
                val x = spacing + i * (barWidth + spacing)
                val baseH = baseHeights.getOrElse(i % baseHeights.size) { 0.5f }

                // Dynamic height variation based on index & animation scale
                val animFactor = if (i % 2 == 0) animScale else (2.0f - animScale)
                val barH = (height * 0.75f * baseH * animFactor).coerceIn(24f, height * 0.95f)

                val isCenterBar = i in 4..8
                val barColor = if (isCenterBar) activeColor.copy(alpha = if (i == 6) 1.0f else 0.85f) else inactiveColor

                drawRoundRect(
                    color = barColor,
                    topLeft = Offset(x, centerY - barH / 2f),
                    size = Size(barWidth, barH),
                    cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
                )
            }
        }
    }
}
