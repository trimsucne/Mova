package co.edu.unal.dadm.mova.presentation.screen.workout

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
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
import co.edu.unal.dadm.mova.R
import co.edu.unal.dadm.mova.core.di.FitnessContainer
import co.edu.unal.dadm.mova.domain.model.WorkoutSessionLog
import co.edu.unal.dadm.mova.presentation.components.MovaPrimaryButton
import co.edu.unal.dadm.mova.presentation.components.StatColumn
import co.edu.unal.dadm.mova.presentation.theme.MovaOrange
import co.edu.unal.dadm.mova.presentation.theme.MovaTheme
import co.edu.unal.dadm.mova.presentation.viewmodel.workout.WorkoutSummaryEvent
import co.edu.unal.dadm.mova.presentation.viewmodel.workout.WorkoutSummaryUiState
import co.edu.unal.dadm.mova.presentation.viewmodel.workout.WorkoutSummaryViewModel

@Composable
fun WorkoutSummaryRoute(sessionId: String, onComplete: () -> Unit) {
    val viewModel: WorkoutSummaryViewModel = viewModel(
        key = "summary_$sessionId",
        factory = viewModelFactory {
            initializer { WorkoutSummaryViewModel(sessionId, FitnessContainer.workoutRepository) }
        }
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    WorkoutSummaryScreen(state) { if (it == WorkoutSummaryEvent.CompleteClicked) onComplete() }
}

@Composable
fun WorkoutSummaryScreen(state: WorkoutSummaryUiState, onEvent: (WorkoutSummaryEvent) -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        Image(
            painter = painterResource(R.drawable.img_male_runner),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(0.2f to Color.Black.copy(alpha = 0.3f), 0.65f to Color.Black.copy(alpha = 0.9f))
            )
        )
        val session = state.session
        if (state.loading || session == null) {
            if (state.loading) CircularProgressIndicator(Modifier.align(Alignment.Center), color = MovaOrange)
            return@Box
        }
        Column(
            modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "${session.workoutTitle}\nComplete", color = Color.White, fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center
            )
            Text(
                "You burned ${session.kcalBurned}kcal", color = Color.White.copy(alpha = 0.85f),
                fontSize = 16.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 10.dp)
            )
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp)) {
                StatColumn("${session.minutes}", "Min", Modifier.weight(1f))
                StatColumn("${session.kcalBurned}", "Kcal", Modifier.weight(1f))
                StatColumn(session.avgBpm?.toString() ?: "--", "BPM", Modifier.weight(1f))
            }
            MovaPrimaryButton(
                text = "Complete",
                onClick = { onEvent(WorkoutSummaryEvent.CompleteClicked) },
                containerColor = MovaOrange
            )
        }
    }
}

private val previewSession = WorkoutSessionLog("s", "back", "Back Workout", 0, 7680, 218, 18, null)

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun WorkoutSummaryPreview() {
    MovaTheme(darkTheme = false) { WorkoutSummaryScreen(WorkoutSummaryUiState(false, previewSession), {}) }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun WorkoutSummaryDarkPreview() {
    MovaTheme(darkTheme = true) { WorkoutSummaryScreen(WorkoutSummaryUiState(false, previewSession), {}) }
}
