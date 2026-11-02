package co.edu.unal.dadm.mova.presentation.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.edu.unal.dadm.mova.presentation.theme.MovaColor

data class WeeklyBarData(
    val dayLabel: String,
    val score: Int,
    val isHighlighted: Boolean = false
)

@Composable
fun BarChartWeekly(
    weeklyData: List<WeeklyBarData>,
    modifier: Modifier = Modifier
) {
    val maxScore = (weeklyData.maxOfOrNull { it.score } ?: 100).coerceAtLeast(100)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "Weekly progress bar chart from Monday to Sunday"
            },
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
        ) {
            val barCount = weeklyData.size.coerceAtLeast(1)
            val availableWidth = size.width
            val barWidth = (availableWidth / barCount) * 0.45f
            val spacing = (availableWidth - (barWidth * barCount)) / (barCount + 1)
            val maxBarHeight = size.height - 20f

            weeklyData.forEachIndexed { index, item ->
                val x = spacing + index * (barWidth + spacing)
                val barHeight = (item.score.toFloat() / maxScore.toFloat()) * maxBarHeight
                val y = size.height - barHeight

                val barColor = if (item.isHighlighted) MovaColor.PrimaryOrange else MovaColor.CardBorder

                drawRoundRect(
                    color = barColor,
                    topLeft = Offset(x, y),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(8f, 8f)
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            weeklyData.forEach { item ->
                Text(
                    text = item.dayLabel,
                    fontSize = 12.sp,
                    fontWeight = if (item.isHighlighted) FontWeight.Bold else FontWeight.Normal,
                    color = if (item.isHighlighted) MovaColor.PrimaryOrange else MovaColor.TextSecondary
                )
            }
        }
    }
}

