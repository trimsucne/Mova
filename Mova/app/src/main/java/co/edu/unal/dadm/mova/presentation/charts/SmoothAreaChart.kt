package co.edu.unal.dadm.mova.presentation.charts

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.edu.unal.dadm.mova.presentation.theme.MovaOrange
import kotlin.math.ceil

/**
 * Curva suavizada con área rellena. Con un solo valor dibuja una línea horizontal.
 *
 * Los parámetros opcionales activan el estilo del mockup (todos desactivados por defecto):
 * [gradientFill] rellena con degradado de [lineColor] a transparente, [gridLevels] (>= 2) dibuja
 * líneas guía con etiquetas del eje Y (0..máximo redondeado), y [highlightIndex] marca un punto
 * con línea vertical y burbuja con [highlightLabel].
 */
@Composable
fun SmoothAreaChart(
    values: List<Float>,
    modifier: Modifier = Modifier,
    lineColor: Color = MovaOrange,
    fillColor: Color = MovaOrange.copy(alpha = 0.2f),
    gradientFill: Boolean = false,
    gridLevels: Int = 0,
    gridColor: Color = Color(0xFFE6E6E8),
    labelColor: Color = Color(0xFF9A9AA0),
    highlightIndex: Int? = null,
    highlightLabel: String? = null
) {
    val measurer = rememberTextMeasurer()
    Canvas(modifier = modifier) {
        if (values.isEmpty()) return@Canvas
        val series = if (values.size == 1) values + values else values
        val styled = gridLevels >= 2
        val hasTooltip = highlightIndex != null && highlightLabel != null
        val rawMax = series.max().coerceAtLeast(1f)

        val steps = (gridLevels - 1).coerceAtLeast(1)
        val niceMax = if (styled) {
            val unit = if (rawMax > 300f) 100f else 10f
            ceil(rawMax / steps / unit).coerceAtLeast(1f) * unit * steps
        } else rawMax

        val leftPad = if (styled) 48.dp.toPx() else 0f
        val topPad = if (hasTooltip) 44.dp.toPx() else if (styled) 8.dp.toPx() else 0f
        val chartW = size.width - leftPad
        val chartH = size.height - topPad
        val stepX = chartW / (series.size - 1)
        fun yOf(value: Float) = topPad + chartH - value / niceMax * chartH
        val points = series.mapIndexed { index, value -> Offset(leftPad + index * stepX, yOf(value)) }

        if (styled) {
            val labelStyle = TextStyle(color = labelColor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            for (level in 0..steps) {
                val value = niceMax / steps * level
                val y = yOf(value)
                drawLine(gridColor, Offset(leftPad, y), Offset(size.width, y), strokeWidth = 1.dp.toPx())
                val text = measurer.measure("%,d".format(value.toInt()), labelStyle)
                drawText(text, topLeft = Offset(leftPad - text.size.width - 8.dp.toPx(), y - text.size.height / 2f))
            }
        }

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
        if (gradientFill) {
            drawPath(area, Brush.verticalGradient(listOf(lineColor.copy(alpha = 0.35f), Color.Transparent), startY = topPad, endY = size.height))
        } else {
            drawPath(area, fillColor)
        }
        drawPath(line, lineColor, style = Stroke(width = 3.dp.toPx()))

        if (hasTooltip && highlightIndex != null) {
            val point = points[highlightIndex.coerceIn(0, points.lastIndex)]
            drawLine(lineColor, Offset(point.x, point.y), Offset(point.x, size.height), strokeWidth = 1.5.dp.toPx())
            drawCircle(Color.White, 6.dp.toPx(), point)
            drawCircle(lineColor, 4.dp.toPx(), point)
            val text = measurer.measure(
                highlightLabel.orEmpty(),
                TextStyle(color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            )
            val padX = 10.dp.toPx()
            val padY = 6.dp.toPx()
            val w = text.size.width + padX * 2
            val h = text.size.height + padY * 2
            val left = (point.x - w / 2).coerceIn(leftPad, size.width - w)
            val top = (point.y - h - 12.dp.toPx()).coerceAtLeast(0f)
            drawRoundRect(lineColor, Offset(left, top), Size(w, h), CornerRadius(10.dp.toPx()))
            drawText(text, topLeft = Offset(left + padX, top + padY))
        }
    }
}
