package co.edu.unal.dadm.mova.presentation.charts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import co.edu.unal.dadm.mova.presentation.theme.MovaInk

@Composable
fun BarChartWeekly(
    score: Int = 95,
    selectedDayIndex: Int = 1,
    weeklyScores: List<Int> = listOf(75, 95, 84, 70, 77, 88, 75),
    modifier: Modifier = Modifier
) {
    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // Y-axis grid lines + chart bars overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
        ) {
            // Background horizontal grid lines (60, 70, 80, 90, 100)
            Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                listOf(100, 90, 80, 70, 60).forEach { level ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = level.toString(),
                            fontSize = 10.sp,
                            color = Color.Gray,
                            modifier = Modifier.width(28.dp)
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(1.dp)
                                .background(Color(0xFFE5E5EA))
                        )
                    }
                }
            }

            // Bars row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(start = 28.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                weeklyScores.forEachIndexed { index, value ->
                    val isSelected = index == selectedDayIndex
                    val barHeightFraction = ((value - 50).toFloat() / 50f).coerceIn(0.15f, 1.0f)

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        if (isSelected) {
                            // Score badge popup box above selected bar
                            Box(
                                modifier = Modifier
                                    .padding(bottom = 4.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MovaInk)
                                    .padding(horizontal = 10.dp, vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = score.toString(),
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .width(12.dp)
                                .fillMaxHeight(barHeightFraction)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) MovaInk else Color(0xFFD1D1D6))
                        )
                    }
                }
            }
        }

        // X-axis Day Labels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 28.dp, top = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            days.forEachIndexed { index, day ->
                val isSelected = index == selectedDayIndex
                Text(
                    text = day,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) MovaInk else Color.Gray
                )
            }
        }
    }
}
