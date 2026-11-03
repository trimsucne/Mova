package co.edu.unal.dadm.mova.presentation.screen.workout

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import co.edu.unal.dadm.mova.core.di.FitnessContainer
import co.edu.unal.dadm.mova.domain.model.WorkoutSeed
import co.edu.unal.dadm.mova.domain.model.WorkoutCategory
import co.edu.unal.dadm.mova.presentation.components.OutlinePill
import co.edu.unal.dadm.mova.presentation.components.TopBarBack
import co.edu.unal.dadm.mova.presentation.theme.MovaInk
import co.edu.unal.dadm.mova.presentation.theme.MovaOrange
import co.edu.unal.dadm.mova.presentation.theme.MovaTheme
import co.edu.unal.dadm.mova.presentation.viewmodel.workout.StrengthCategoryEvent
import co.edu.unal.dadm.mova.presentation.viewmodel.workout.StrengthCategoryUiState
import co.edu.unal.dadm.mova.presentation.viewmodel.workout.StrengthCategoryViewModel

@Composable
fun StrengthCategoryRoute(
    category: WorkoutCategory,
    onWorkoutClick: (String) -> Unit,
    onBack: () -> Unit
) {
    val viewModel: StrengthCategoryViewModel = viewModel(
        key = "category_${category.id}",
        factory = viewModelFactory {
            initializer { StrengthCategoryViewModel(category, FitnessContainer.observeWorkouts) }
        }
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    StrengthCategoryScreen(state) { event ->
        when (event) {
            is StrengthCategoryEvent.WorkoutClicked -> onWorkoutClick(event.workoutId)
            StrengthCategoryEvent.BackClicked -> onBack()
        }
        viewModel.onEvent(event)
    }
}

@Composable
fun StrengthCategoryScreen(state: StrengthCategoryUiState, onEvent: (StrengthCategoryEvent) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(MovaInk)) {
        Column(modifier = Modifier.statusBarsPadding().padding(horizontal = 20.dp, vertical = 8.dp)) {
            TopBarBack(onBack = { onEvent(StrengthCategoryEvent.BackClicked) }, foreground = Color.White)
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(state.category.label, color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)
                OutlinePill("${state.workouts.size} Total")
            }
            Text(
                state.category.description,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 12.dp, bottom = 20.dp)
            )
        }
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("All Workouts", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("See All", color = MovaOrange, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
                when {
                    state.loading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator(color = MovaOrange) }
                    state.error != null -> Text(state.error, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 16.dp))
                    state.workouts.isEmpty() -> Text(
                        "No workouts in this category yet.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                    else -> LazyColumn(
                        modifier = Modifier.padding(top = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(state.workouts, key = { it.id }) { workout ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(28.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { onEvent(StrengthCategoryEvent.WorkoutClicked(workout.id)) }
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Image(
                                    painter = painterResource(workoutImage(workout.imageKey)),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.size(88.dp).clip(RoundedCornerShape(20.dp))
                                )
                                Column(modifier = Modifier.padding(start = 16.dp)) {
                                    Text(workout.title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                    Text(
                                        "${workout.totalExercises} Total",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                    Text(
                                        "${workout.reps}x reps Each",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun StrengthCategoryPreview() {
    MovaTheme(darkTheme = false) {
        StrengthCategoryScreen(
            StrengthCategoryUiState(loading = false, workouts = WorkoutSeed.workouts.take(4)),
            onEvent = {}
        )
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun StrengthCategoryDarkPreview() {
    MovaTheme(darkTheme = true) {
        StrengthCategoryScreen(
            StrengthCategoryUiState(loading = false, workouts = WorkoutSeed.workouts.take(4)),
            onEvent = {}
        )
    }
}
