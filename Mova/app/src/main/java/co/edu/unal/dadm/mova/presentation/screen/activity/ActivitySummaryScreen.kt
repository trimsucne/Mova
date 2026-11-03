package co.edu.unal.dadm.mova.presentation.screen.activity

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import co.edu.unal.dadm.mova.core.common.GeoUtils
import co.edu.unal.dadm.mova.core.di.FitnessContainer
import co.edu.unal.dadm.mova.domain.model.ActivityLog
import co.edu.unal.dadm.mova.domain.model.ActivityType
import co.edu.unal.dadm.mova.presentation.charts.DonutChart
import co.edu.unal.dadm.mova.presentation.charts.DonutSlice
import co.edu.unal.dadm.mova.presentation.components.MovaPrimaryButton
import co.edu.unal.dadm.mova.presentation.components.TopBarBack
import co.edu.unal.dadm.mova.presentation.components.formatClock
import co.edu.unal.dadm.mova.presentation.theme.MovaInk
import co.edu.unal.dadm.mova.presentation.theme.MovaOrange
import co.edu.unal.dadm.mova.presentation.theme.MovaTheme
import co.edu.unal.dadm.mova.presentation.viewmodel.activity.ActivitySummaryEvent
import co.edu.unal.dadm.mova.presentation.viewmodel.activity.ActivitySummaryUiState
import co.edu.unal.dadm.mova.presentation.viewmodel.activity.ActivitySummaryViewModel

private val CalorieColor = Color(0xFFA37CF0)
private val BpmColor = Color(0xFF3D3E40)

@Composable
fun ActivitySummaryRoute(logId: String, onDone: () -> Unit, onBack: () -> Unit) {
    val viewModel: ActivitySummaryViewModel = viewModel(
        key = "act_summary_$logId",
        factory = viewModelFactory { initializer { ActivitySummaryViewModel(logId, FitnessContainer.activityRepository) } }
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    ActivitySummaryScreen(state) { event ->
        when (event) {
            ActivitySummaryEvent.DoneClicked -> onDone()
            ActivitySummaryEvent.BackClicked -> onBack()
        }
    }
}

/**
 * Composicion del donut: cada metrica se normaliza contra una referencia
 * (5 km, 300 kcal, 160 bpm) y se muestra su peso relativo.
 */
internal fun donutSlices(log: ActivityLog): List<DonutSlice> {
    val distance = (log.distanceMeters / 5000.0).coerceIn(0.05, 1.0).toFloat()
    val calories = (log.kcal / 300.0).coerceIn(0.05, 1.0).toFloat()
    val bpm = ((log.avgBpm ?: 120) / 160.0).coerceIn(0.05, 1.0).toFloat()
    return listOf(
        DonutSlice(distance, MovaOrange),
        DonutSlice(calories, CalorieColor),
        DonutSlice(bpm, BpmColor)
    )
}

@Composable
fun ActivitySummaryScreen(state: ActivitySummaryUiState, onEvent: (ActivitySummaryEvent) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TopBarBack(onBack = { onEvent(ActivitySummaryEvent.BackClicked) })
        val log = state.log
        if (log == null) {
            Box(Modifier.weight(1f).fillMaxWidth(), Alignment.Center) {
                if (state.loading) CircularProgressIndicator(color = MovaOrange) else Text("Activity not found")
            }
            return@Column
        }
        Text("${log.type.label} Completed", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp))
        DonutChart(slices = donutSlices(log), modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(20.dp), verticalAlignment = Alignment.CenterVertically) {
            LegendDot("Distance", MovaOrange)
            LegendDot("Calorie", CalorieColor)
            LegendDot("BPM", BpmColor)
        }
        Row(Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            Metric(GeoUtils.formatDistance(log.distanceMeters), "Distance")
            Metric("${log.kcal}", "Kcal")
            Metric(formatClock(log.durationSec), "Time")
        }
        Row(
            modifier = Modifier
                .fillMaxWidth().padding(top = 20.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text("Post ${log.type.label} Stretch", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                // Texto estatico hasta integrar sugerencias del Coach IA (Dev D).
                Text("+12 More AI Suggestions", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Box(Modifier.size(56.dp).clip(RoundedCornerShape(16.dp)).background(MovaOrange), contentAlignment = Alignment.Center) {
                Text(log.type.emoji(), fontSize = 28.sp)
            }
        }
        Box(Modifier.weight(1f))
        MovaPrimaryButton("Great, Thanks", { onEvent(ActivitySummaryEvent.DoneClicked) }, trailingText = null,
            containerColor = MaterialTheme.colorScheme.secondary, contentColor = MaterialTheme.colorScheme.onSecondary,
            modifier = Modifier.padding(vertical = 12.dp))
    }
}

@Composable
private fun LegendDot(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(16.dp).clip(CircleShape).background(color))
        Text(label, fontWeight = FontWeight.Bold, fontSize = 15.sp, modifier = Modifier.padding(start = 6.dp))
    }
}

@Composable
private fun Metric(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onBackground)
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private val sampleLog = ActivityLog("1", ActivityType.Jogging, 0, 1860, 4200.0, 252, 138)

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun ActivitySummaryPreview() {
    MovaTheme(darkTheme = false) { ActivitySummaryScreen(ActivitySummaryUiState(false, sampleLog), {}) }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun ActivitySummaryDarkPreview() {
    MovaTheme(darkTheme = true) { ActivitySummaryScreen(ActivitySummaryUiState(false, sampleLog), {}) }
}
