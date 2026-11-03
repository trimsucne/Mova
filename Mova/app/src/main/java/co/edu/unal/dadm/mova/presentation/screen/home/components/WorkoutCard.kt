package co.edu.unal.dadm.mova.presentation.screen.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import co.edu.unal.dadm.mova.domain.model.Workout
import co.edu.unal.dadm.mova.presentation.components.MovaIcons
import co.edu.unal.dadm.mova.presentation.theme.MovaColor

@Composable
fun WorkoutCard(
    workouts: List<Workout>,
    onWorkoutClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    onExploreClick: () -> Unit = {}
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Workouts 🏋️‍♂️",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MovaColor.TextBlack
            )
            Text(
                text = "Explore",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MovaColor.PrimaryOrange,
                modifier = Modifier.clickable(onClick = onExploreClick)
            )
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(workouts) { workout ->
                Card(
                    modifier = Modifier
                        .width(200.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onWorkoutClick(workout.id) },
                    colors = CardDefaults.cardColors(containerColor = MovaColor.CardLightGray)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MovaColor.PrimaryOrangeLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = MovaIcons.Dumbbell,
                                contentDescription = null,
                                tint = MovaColor.PrimaryOrange,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Text(
                            text = workout.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MovaColor.TextBlack
                        )
                        Text(
                            text = "${workout.durationMinutes} min • ${workout.caloriesBurned} kcal",
                            fontSize = 12.sp,
                            color = MovaColor.TextSecondary
                        )
                    }
                }
            }
        }
    }
}

