package co.edu.unal.dadm.mova.presentation.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

@Composable
fun RulerPicker(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    range: ClosedRange<Float> = 30f..180f,
    step: Float = 1f,
    lineColor: Color = Color.White,
    centerIndicatorColor: Color = Color.White
) {
    val textMeasurer = rememberTextMeasurer()
    val pxPerUnit = 24f
    val currentVal by rememberUpdatedState(value)
    val currentOnValueChange by rememberUpdatedState(onValueChange)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(110.dp)
            .pointerInput(Unit) {
                var accumulatedDrag = 0f
                detectHorizontalDragGestures { _, dragAmount ->
                    accumulatedDrag += -dragAmount
                    val unitsMoved = accumulatedDrag / pxPerUnit
                    if (kotlin.math.abs(unitsMoved) >= 0.2f) {
                        val newValue = (currentVal + unitsMoved).coerceIn(range.start, range.endInclusive)
                        val roundedValue = (newValue / step).roundToInt() * step
                        if (roundedValue != currentVal) {
                            currentOnValueChange(roundedValue)
                        }
                        accumulatedDrag = 0f
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxWidth().height(110.dp)) {
            val width = size.width
            val height = size.height
            val centerX = width / 2f
            val baseLineY = height - 20f

            val minVal = range.start.toInt()
            val maxVal = range.endInclusive.toInt()

            for (i in minVal..maxVal) {
                val offsetFromCenter = (i - value) * pxPerUnit
                val x = centerX + offsetFromCenter

                if (x >= -40f && x <= width + 40f) {
                    val isMajor = i % 5 == 0
                    val lineLength = if (isMajor) 48f else 28f
                    val strokeWidth = if (isMajor) 3f else 2f

                    drawLine(
                        color = lineColor.copy(alpha = if (isMajor) 0.95f else 0.6f),
                        start = Offset(x, baseLineY),
                        end = Offset(x, baseLineY - lineLength),
                        strokeWidth = strokeWidth
                    )

                    if (isMajor) {
                        val textLayout = textMeasurer.measure(
                            text = i.toString(),
                            style = TextStyle(
                                color = lineColor.copy(alpha = 0.95f),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        drawText(
                            textLayoutResult = textLayout,
                            topLeft = Offset(
                                x = x - textLayout.size.width / 2f,
                                y = baseLineY + 4f
                            )
                        )
                    }
                }
            }

            // Central highlighted marker line
            drawLine(
                color = centerIndicatorColor,
                start = Offset(centerX, baseLineY + 12f),
                end = Offset(centerX, baseLineY - 62f),
                strokeWidth = 6f
            )
        }
    }
}
