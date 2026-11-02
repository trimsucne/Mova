package co.edu.unal.dadm.mova.presentation.screen.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.edu.unal.dadm.mova.presentation.theme.MovaColor

@Composable
fun CalorieGoalCard(
    consumedCalories: Int,
    calorieGoal: Int,
    remainingCalories: Int,
    modifier: Modifier = Modifier
) {
    val progress = if (calorieGoal > 0) (consumedCalories.toFloat() / calorieGoal.toFloat()).coerceIn(0f, 1f) else 0f

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = MovaColor.CardLightGray)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Calorie Goal",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MovaColor.TextBlack
                )
                Text(
                    text = "$remainingCalories kcal remaining",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MovaColor.PrimaryOrange
                )
            }

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color = MovaColor.PrimaryOrange,
                trackColor = MovaColor.CardBorder
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Consumed", fontSize = 12.sp, color = MovaColor.TextSecondary)
                    Text("$consumedCalories kcal", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MovaColor.TextBlack)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Daily Goal", fontSize = 12.sp, color = MovaColor.TextSecondary)
                    Text("$calorieGoal kcal", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MovaColor.TextBlack)
                }
            }
        }
    }
}

