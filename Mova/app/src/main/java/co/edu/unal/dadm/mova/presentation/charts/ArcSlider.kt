package co.edu.unal.dadm.mova.presentation.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import co.edu.unal.dadm.mova.presentation.theme.MovaInk
import co.edu.unal.dadm.mova.presentation.theme.MovaOrange
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun ArcSlider(
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    range: IntRange = 1..5,
    activeColor: Color = MovaOrange,
    inactiveColor: Color = MovaInk
) {
    val totalSteps = range.last - range.first
    val minAngle = 180f
    val maxAngle = 360f
    val sweepTotal = maxAngle - minAngle

    val currentVal by rememberUpdatedState(value)
    val currentOnValueChange by rememberUpdatedState(onValueChange)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(240.dp)
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    val pos = change.position
                    val width = size.width
                    val height = size.height
                    val center = Offset(width / 2f, height * 0.85f)
                    val dx = pos.x - center.x
                    val dy = pos.y - center.y
                    var angle = (atan2(dy, dx) * 180f / PI).toFloat()
                    if (angle < 0) angle += 360f

                    if (angle in (minAngle - 10f)..(maxAngle + 10f)) {
                        val clampedAngle = angle.coerceIn(minAngle, maxAngle)
                        val fraction = (clampedAngle - minAngle) / sweepTotal
                        val newLevel = (range.first + (fraction * totalSteps).roundToInt()).coerceIn(range.first, range.last)
                        if (newLevel != currentVal) {
                            currentOnValueChange(newLevel)
                        }
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxWidth().height(240.dp)) {
            val width = size.width
            val height = size.height
            val center = Offset(width / 2f, height * 0.85f)
            val radius = width * 0.38f

            val strokeWidth = 16f

            val currentFraction = (value - range.first).toFloat() / totalSteps.coerceAtLeast(1)
            val activeSweep = currentFraction * sweepTotal

            // Inactive arc track
            drawArc(
                color = inactiveColor.copy(alpha = 0.85f),
                startAngle = minAngle,
                sweepAngle = sweepTotal,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Active arc highlight
            if (activeSweep > 0) {
                drawArc(
                    color = activeColor,
                    startAngle = minAngle,
                    sweepAngle = activeSweep,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidth + 4f, cap = StrokeCap.Round)
                )
            }

            // Radial tick marks
            for (step in 0..totalSteps) {
                val stepAngle = minAngle + (step.toFloat() / totalSteps) * sweepTotal
                val rad = stepAngle * (PI / 180f)
                val innerR = radius - 22f
                val outerR = radius + 22f

                val startTick = Offset(
                    (center.x + innerR * cos(rad)).toFloat(),
                    (center.y + innerR * sin(rad)).toFloat()
                )
                val endTick = Offset(
                    (center.x + outerR * cos(rad)).toFloat(),
                    (center.y + outerR * sin(rad)).toFloat()
                )

                drawLine(
                    color = if (step <= (value - range.first)) activeColor else inactiveColor.copy(alpha = 0.4f),
                    start = startTick,
                    end = endTick,
                    strokeWidth = 3.5f
                )
            }

            // Thumb Handle
            val currentAngle = minAngle + activeSweep
            val thumbRad = currentAngle * (PI / 180f)
            val thumbCenter = Offset(
                (center.x + radius * cos(thumbRad)).toFloat(),
                (center.y + radius * sin(thumbRad)).toFloat()
            )

            val thumbSize = 50f
            drawRoundRect(
                color = activeColor,
                topLeft = Offset(thumbCenter.x - thumbSize / 2, thumbCenter.y - thumbSize / 2),
                size = Size(thumbSize, thumbSize),
                cornerRadius = CornerRadius(16f, 16f)
            )

            // Outer ring inside thumb
            drawCircle(
                color = Color.White,
                radius = 11f,
                center = thumbCenter,
                style = Stroke(width = 4f)
            )
        }
    }
}
