package co.edu.unal.dadm.mova.presentation.charts

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import co.edu.unal.dadm.mova.presentation.theme.MovaOrange

/** Curva suavizada con área rellena. Con un solo valor dibuja una línea horizontal. */
@Composable
fun SmoothAreaChart(
    values: List<Float>,
    modifier: Modifier = Modifier,
    lineColor: Color = MovaOrange,
    fillColor: Color = MovaOrange.copy(alpha = 0.2f)
) {
    Canvas(modifier = modifier) {
        if (values.isEmpty()) return@Canvas
        val series = if (values.size == 1) values + values else values
        val max = series.max().coerceAtLeast(1f)
        val stepX = size.width / (series.size - 1)
        val points = series.mapIndexed { index, value -> Offset(index * stepX, size.height - value / max * size.height) }

        val line = Path().apply {
            moveTo(points.first().x, points.first().y)
            for (i in 1 until points.size) {
                val previous = points[i - 1]
                val current = points[i]
                val midX = (previous.x + current.x) / 2
                cubicTo(midX, previous.y, midX, current.y, current.x, current.y)
            }
        }
        val area = Path().apply {
            addPath(line)
            lineTo(points.last().x, size.height)
            lineTo(points.first().x, size.height)
            close()
        }
        drawPath(area, fillColor)
        drawPath(line, lineColor, style = Stroke(width = 3.dp.toPx()))
    }
}
