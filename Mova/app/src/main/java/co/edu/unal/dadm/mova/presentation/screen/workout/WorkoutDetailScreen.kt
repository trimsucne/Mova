package co.edu.unal.dadm.mova.presentation.screen.workout

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import co.edu.unal.dadm.mova.core.di.FitnessContainer
import co.edu.unal.dadm.mova.domain.model.WorkoutSeed
import co.edu.unal.dadm.mova.presentation.components.OutlinePill
import co.edu.unal.dadm.mova.presentation.components.StatColumn
import co.edu.unal.dadm.mova.presentation.components.TopBarBack
import co.edu.unal.dadm.mova.presentation.theme.MovaOrange
import co.edu.unal.dadm.mova.presentation.theme.MovaTheme
import co.edu.unal.dadm.mova.presentation.viewmodel.workout.WorkoutDetailEvent
import co.edu.unal.dadm.mova.presentation.viewmodel.workout.WorkoutDetailUiState
import co.edu.unal.dadm.mova.presentation.viewmodel.workout.WorkoutDetailViewModel

@Composable
fun WorkoutDetailRoute(workoutId: String, onStart: () -> Unit, onBack: () -> Unit) {
    val viewModel: WorkoutDetailViewModel = viewModel(
        key = "detail_$workoutId",
        factory = viewModelFactory {
            initializer { WorkoutDetailViewModel(workoutId, FitnessContainer.workoutRepository) }
        }
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    WorkoutDetailScreen(state) { event ->
        when (event) {
            WorkoutDetailEvent.StartClicked -> onStart()
            WorkoutDetailEvent.BackClicked -> onBack()
            else -> viewModel.onEvent(event)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutDetailScreen(state: WorkoutDetailUiState, onEvent: (WorkoutDetailEvent) -> Unit) {
    val workout = state.workout
    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        when {
            state.loading -> CircularProgressIndicator(Modifier.align(Alignment.Center), color = MovaOrange)
            workout == null -> Column(
                Modifier.align(Alignment.Center).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Workout not found", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            else -> {
                Image(
                    painter = painterResource(workoutImage(workout.imageKey)),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    Modifier.fillMaxSize().background(
                        Brush.verticalGradient(
                            0.35f to Color.Transparent,
                            1f to Color.Black.copy(alpha = 0.92f)
                        )
                    )
                )
                Column(
                    modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    OutlinePill("${workout.totalExercises} Total")
                    Text(
                        workout.title, color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center, modifier = Modifier.padding(top = 20.dp)
                    )
                    workout.trainer?.let {
                        Text("With $it", color = Color.White.copy(alpha = 0.7f), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min).padding(vertical = 28.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StatColumn("${workout.durationMin}min", "Time", Modifier.weight(1f))
                        Box(Modifier.width(1.dp).fillMaxHeight().background(Color.White.copy(alpha = 0.3f)))
                        StatColumn("${workout.kcal}kcal", "Calorie", Modifier.weight(1f))
                        Box(Modifier.width(1.dp).fillMaxHeight().background(Color.White.copy(alpha = 0.3f)))
                        StatColumn("${workout.sets}*${workout.reps}", "Sets", Modifier.weight(1f))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        OutlinedButton(
                            onClick = { onEvent(WorkoutDetailEvent.DetailsClicked) },
                            modifier = Modifier.weight(1f).height(54.dp),
                            shape = RoundedCornerShape(18.dp),
                            border = BorderStroke(1.dp, Color.White),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                        ) { Text("Details", fontWeight = FontWeight.Bold, fontSize = 16.sp) }
                        Button(
                            onClick = { onEvent(WorkoutDetailEvent.StartClicked) },
                            modifier = Modifier.weight(1f).height(54.dp),
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MovaOrange, contentColor = Color.White)
                        ) { Text("Start", fontWeight = FontWeight.Bold, fontSize = 16.sp) }
                    }
                }
            }
        }
        TopBarBack(
            onBack = { onEvent(WorkoutDetailEvent.BackClicked) },
            foreground = Color.White,
            modifier = Modifier.align(Alignment.TopStart).statusBarsPadding().padding(horizontal = 20.dp)
        )
    }

    if (state.showDetails && workout != null) {
        ModalBottomSheet(onDismissRequest = { onEvent(WorkoutDetailEvent.DetailsDismissed) }) {
            Column(modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 32.dp)) {
                Text("${workout.title} · exercises", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                workout.exercises.forEach { ex ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(ex.name, fontWeight = FontWeight.SemiBold)
                        Text(
                            "${ex.sets} x ${ex.reps}",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun WorkoutDetailPreview() {
    MovaTheme(darkTheme = false) {
        WorkoutDetailScreen(WorkoutDetailUiState(loading = false, workout = WorkoutSeed.workouts.first()), onEvent = {})
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun WorkoutDetailDarkPreview() {
    MovaTheme(darkTheme = true) {
        WorkoutDetailScreen(WorkoutDetailUiState(loading = false, workout = WorkoutSeed.workouts.first()), onEvent = {})
    }
}
