package co.edu.unal.dadm.mova.presentation.screen.workout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import co.edu.unal.dadm.mova.presentation.components.TopBarBack
import co.edu.unal.dadm.mova.presentation.components.formatClock
import co.edu.unal.dadm.mova.presentation.theme.MovaInk
import co.edu.unal.dadm.mova.presentation.theme.MovaOrange
import co.edu.unal.dadm.mova.presentation.theme.MovaTheme
import co.edu.unal.dadm.mova.presentation.viewmodel.workout.WorkoutSessionEvent
import co.edu.unal.dadm.mova.presentation.viewmodel.workout.WorkoutSessionUiState
import co.edu.unal.dadm.mova.presentation.viewmodel.workout.WorkoutSessionViewModel
import kotlinx.coroutines.delay

@Composable
fun WorkoutSessionRoute(workoutId: String, onFinished: (sessionId: String) -> Unit, onBack: () -> Unit) {
    val viewModel: WorkoutSessionViewModel = viewModel(
        key = "session_$workoutId",
        factory = viewModelFactory {
            initializer {
                WorkoutSessionViewModel(workoutId, FitnessContainer.workoutRepository, FitnessContainer.completeWorkoutSession)
            }
        }
    )
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Pulso de 1 s mientras la pantalla esta en composicion; el estado vive en el ViewModel.
    LaunchedEffect(Unit) {
        while (true) {
            delay(1_000)
            viewModel.onEvent(WorkoutSessionEvent.Tick)
        }
    }
    LaunchedEffect(state.savedSessionId) { state.savedSessionId?.let(onFinished) }

    WorkoutSessionScreen(state) { event ->
        if (event == WorkoutSessionEvent.BackClicked) onBack() else viewModel.onEvent(event)
    }
}

@Composable
fun WorkoutSessionScreen(state: WorkoutSessionUiState, onEvent: (WorkoutSessionEvent) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().background(MovaInk).statusBarsPadding().navigationBarsPadding().padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TopBarBack(onBack = { onEvent(WorkoutSessionEvent.BackClicked) }, foreground = Color.White)
            Spacer(Modifier.weight(1f))
            Text(formatClock(state.elapsedSec), color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.weight(1f))
            Spacer(Modifier.padding(22.dp))
        }
        val workout = state.workout
        if (state.loading || workout == null) {
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                if (state.loading) CircularProgressIndicator(color = MovaOrange)
                else Text("Workout not found", color = Color.White)
            }
            return@Column
        }
        Text(workout.title, color = Color.White.copy(alpha = 0.7f), fontSize = 16.sp, fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 8.dp))
        LinearProgressIndicator(
            progress = { state.progress },
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp).height(8.dp),
            color = MovaOrange,
            trackColor = Color.White.copy(alpha = 0.15f)
        )
        Text("${state.completedSets} / ${state.totalSets} sets", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp,
            modifier = Modifier.padding(top = 6.dp))

        Column(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val exercise = state.currentExercise
            when {
                state.allDone -> {
                    Text("All sets complete!", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center)
                    Text("Great job. Save your session.", color = Color.White.copy(alpha = 0.7f), modifier = Modifier.padding(top = 8.dp))
                }
                state.resting -> {
                    Text("REST", color = MovaOrange, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("${state.restRemainingSec}", color = Color.White, fontSize = 96.sp, fontWeight = FontWeight.ExtraBold)
                    Text(
                        "Next: ${exercise?.name ?: ""}", color = Color.White.copy(alpha = 0.7f),
                        fontSize = 16.sp, fontWeight = FontWeight.SemiBold
                    )
                    OutlinedButton(
                        onClick = { onEvent(WorkoutSessionEvent.SkipRest) },
                        modifier = Modifier.padding(top = 24.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) { Text("Skip rest") }
                }
                exercise != null -> {
                    Text(
                        "Exercise ${state.exerciseIndex + 1} of ${workout.totalExercises}",
                        color = Color.White.copy(alpha = 0.6f), fontSize = 14.sp
                    )
                    Text(
                        exercise.name, color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center, modifier = Modifier.padding(top = 8.dp)
                    )
                    Text(
                        "Set ${state.setsDoneInExercise + 1} of ${exercise.sets} · ${exercise.reps} reps",
                        color = MovaOrange, fontSize = 18.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                    Button(
                        onClick = { onEvent(WorkoutSessionEvent.CompleteSet) },
                        enabled = !state.paused,
                        modifier = Modifier.padding(top = 32.dp).fillMaxWidth().height(60.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MovaOrange, contentColor = Color.White)
                    ) { Text("Set done", fontSize = 18.sp, fontWeight = FontWeight.Bold) }
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            if (!state.allDone) {
                OutlinedButton(
                    onClick = { onEvent(WorkoutSessionEvent.TogglePause) },
                    modifier = Modifier.weight(1f).height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) { Text(if (state.paused) "Resume" else "Pause", fontWeight = FontWeight.Bold) }
            }
            Button(
                onClick = { onEvent(WorkoutSessionEvent.FinishClicked) },
                enabled = !state.saving,
                modifier = Modifier.weight(1f).height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (state.allDone) MovaOrange else Color.White,
                    contentColor = if (state.allDone) Color.White else MovaInk
                )
            ) { Text(if (state.allDone) "Finish" else "Finish early", fontWeight = FontWeight.Bold) }
        }
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun WorkoutSessionPreview() {
    MovaTheme(darkTheme = false) {
        WorkoutSessionScreen(
            WorkoutSessionUiState(loading = false, workout = WorkoutSeed.workouts.first(), completedSets = 2, setsDoneInExercise = 2, elapsedSec = 312),
            onEvent = {}
        )
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun WorkoutSessionRestPreview() {
    MovaTheme(darkTheme = true) {
        WorkoutSessionScreen(
            WorkoutSessionUiState(
                loading = false, workout = WorkoutSeed.workouts.first(), completedSets = 3,
                exerciseIndex = 1, resting = true, restRemainingSec = 21, elapsedSec = 400
            ),
            onEvent = {}
        )
    }
}
