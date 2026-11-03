package co.edu.unal.dadm.mova.presentation.screen.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.edu.unal.dadm.mova.domain.model.MealLog
import co.edu.unal.dadm.mova.presentation.components.MovaIcons
import co.edu.unal.dadm.mova.presentation.theme.MovaColor

@Composable
fun DietCard(
    meals: List<MealLog>,
    onSeeAllClick: () -> Unit,
    modifier: Modifier = Modifier
) {
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
                    text = "Diet & Nutrition 🥗",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MovaColor.TextBlack
                )
                Text(
                    text = "See All",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MovaColor.PrimaryOrange,
                    modifier = Modifier.clickable { onSeeAllClick() }
                )
            }

            if (meals.isEmpty()) {
                Text(
                    text = "No meals logged today yet.",
                    fontSize = 13.sp,
                    color = MovaColor.TextSecondary
                )
            } else {
                meals.take(3).forEach { meal ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MovaColor.CalorieLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = MovaIcons.Flame,
                                    contentDescription = null,
                                    tint = MovaColor.CalorieRed,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = meal.name,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MovaColor.TextBlack
                                )
                                Text(
                                    text = "P: ${meal.macros.proteinG.toInt()}g • C: ${meal.macros.carbsG.toInt()}g • F: ${meal.macros.fatG.toInt()}g",
                                    fontSize = 12.sp,
                                    color = MovaColor.TextSecondary
                                )
                            }
                        }
                        Text(
                            text = "${meal.kcal} kcal",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MovaColor.TextBlack
                        )
                    }
                }
            }
        }
    }
}

