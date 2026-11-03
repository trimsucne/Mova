package co.edu.unal.dadm.mova.presentation.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.edu.unal.dadm.mova.presentation.theme.MovaInk
import co.edu.unal.dadm.mova.presentation.theme.MovaOrange
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

data class DonutSlice(val value: Float, val color: Color)

/** Dona con burbujas de porcentaje blancas sobre cada segmento (mockup "Jogging Completed"). */
@Composable
fun DonutChart(
    slices: List<DonutSlice>,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 64.dp,
    showPercent: Boolean = true
) {
    val total = slices.sumOf { it.value.toDouble() }.toFloat().takeIf { it > 0f } ?: 1f
    BoxWithConstraints(modifier = modifier.aspectRatio(1f)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = strokeWidth.toPx()
            var start = -90f
            slices.forEach { s ->
                val sweep = 360f * s.value / total
                drawArc(
                    color = s.color,
                    startAngle = start,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = Offset(stroke / 2, stroke / 2),
                    size = Size(size.width - stroke, size.height - stroke),
                    style = Stroke(width = stroke)
                )
                start += sweep
            }
        }
        if (showPercent) {
            val radius = maxWidth / 2 - strokeWidth / 2
            var start = -90f
            slices.forEach { s ->
                val sweep = 360f * s.value / total
                val mid = Math.toRadians((start + sweep / 2).toDouble())
                val dx = radius * cos(mid).toFloat()
                val dy = radius * sin(mid).toFloat()
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(dx, dy)
                        .background(Color.White, CircleShape)
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "${(100f * s.value / total).roundToInt()}%",
                        color = MovaInk,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                start += sweep
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DonutChartPreview() {
    DonutChart(
        slices = listOf(
            DonutSlice(26f, MovaOrange),
            DonutSlice(24f, Color(0xFFA37CF0)),
            DonutSlice(54f, Color(0xFF3D3E40))
        ),
        modifier = Modifier.padding(24.dp)
    )
}
