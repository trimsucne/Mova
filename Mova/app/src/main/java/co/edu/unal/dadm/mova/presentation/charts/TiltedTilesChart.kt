package co.edu.unal.dadm.mova.presentation.charts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.edu.unal.dadm.mova.presentation.theme.MovaInk
import co.edu.unal.dadm.mova.presentation.theme.MovaOrange
import kotlin.math.sqrt

data class TileItem(val hours: Double, val color: Color, val textColor: Color = Color.White)

private data class Slot(val x: Float, val y: Float, val rotation: Float)

private val Slots = listOf(
    Slot(0.50f, 0.50f, 35f),
    Slot(0.12f, 0.16f, -70f),
    Slot(0.84f, 0.20f, 35f),
    Slot(0.18f, 0.86f, -30f),
    Slot(0.82f, 0.88f, -12f)
)

/** Fichas inclinadas cuyo tamano es proporcional a las horas (mockup "Activity Status"). */
@Composable
fun TiltedTilesChart(items: List<TileItem>, modifier: Modifier = Modifier) {
    val top = items.sortedByDescending { it.hours }.take(Slots.size)
    val maxHours = top.firstOrNull()?.hours?.takeIf { it > 0 } ?: return
    BoxWithConstraints(modifier = modifier.fillMaxWidth().height(430.dp)) {
        val w = maxWidth
        val h = maxHeight
        top.forEachIndexed { i, item ->
            val slot = Slots[i]
            val scale = sqrt(item.hours / maxHours).toFloat().coerceIn(0.38f, 1f)
            val tileW = w * 0.62f * scale
            val tileH = tileW * 0.62f
            Box(
                modifier = Modifier
                    .offset(w * slot.x - tileW / 2, h * slot.y - tileH / 2)
                    .size(tileW, tileH)
                    .rotate(slot.rotation)
                    .background(item.color, RoundedCornerShape(28.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = formatHours(item.hours),
                    color = item.textColor,
                    fontSize = (30 * scale + 12).sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}

private fun formatHours(h: Double): String =
    if (h >= 10) "${h.toInt()}h" else if (h >= 1) String.format("%.1fh", h) else "${(h * 60).toInt()}m"

@Preview(showBackground = true)
@Composable
private fun TiltedTilesPreview() {
    TiltedTilesChart(
        items = listOf(
            TileItem(68.0, MovaInk),
            TileItem(7.0, Color(0xFFEF4444)),
            TileItem(1.0, MovaOrange),
            TileItem(87.0, Color(0xFFEDEDED), MovaInk),
            TileItem(15.0, Color(0xFF2563EB))
        )
    )
}
