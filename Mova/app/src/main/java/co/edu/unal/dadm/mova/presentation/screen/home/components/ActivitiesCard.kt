package co.edu.unal.dadm.mova.presentation.screen.home.components

import androidx.compose.foundation.background
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
import co.edu.unal.dadm.mova.domain.model.ActivityLog
import co.edu.unal.dadm.mova.presentation.components.MovaIcons
import co.edu.unal.dadm.mova.presentation.theme.MovaColor

@Composable
fun ActivitiesCard(
    activities: List<ActivityLog>,
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
            Text(
                text = "Today's Activities 🏃",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MovaColor.TextBlack
            )

            if (activities.isEmpty()) {
                Text(
                    text = "No activities logged today.",
                    fontSize = 13.sp,
                    color = MovaColor.TextSecondary
                )
            } else {
                activities.forEach { activity ->
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
                                    .background(MovaColor.ScoreGreenLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = MovaIcons.Star,
                                    contentDescription = null,
                                    tint = MovaColor.ScoreGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = activity.title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MovaColor.TextBlack
                                )
                                Text(
                                    text = "${activity.durationMinutes} mins • ${activity.distanceKm} km",
                                    fontSize = 12.sp,
                                    color = MovaColor.TextSecondary
                                )
                            }
                        }
                        Text(
                            text = "${activity.caloriesBurned} kcal",
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

