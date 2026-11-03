package co.edu.unal.dadm.mova.presentation.screen.activity

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Box
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import co.edu.unal.dadm.mova.core.di.FitnessContainer
import co.edu.unal.dadm.mova.domain.model.ActivityLog
import co.edu.unal.dadm.mova.domain.model.ActivityType
import co.edu.unal.dadm.mova.presentation.charts.TileItem
import co.edu.unal.dadm.mova.presentation.charts.TiltedTilesChart
import co.edu.unal.dadm.mova.presentation.components.MovaPrimaryButton
import co.edu.unal.dadm.mova.presentation.components.TopBarBack
import co.edu.unal.dadm.mova.presentation.theme.MovaOrange
import co.edu.unal.dadm.mova.presentation.theme.MovaTheme
import co.edu.unal.dadm.mova.presentation.viewmodel.activity.ActivityStatusEvent
import co.edu.unal.dadm.mova.presentation.viewmodel.activity.ActivityStatusUiState
import co.edu.unal.dadm.mova.presentation.viewmodel.activity.ActivityStatusViewModel
import java.text.DateFormat
import java.util.Date

/** Punto de entrada del modulo Actividades: vacio -> NoActivities, con datos -> Activity Status. */
@Composable
fun ActivityStatusRoute(onAdd: () -> Unit, onActivityClick: (String) -> Unit, onBack: () -> Unit) {
    val viewModel: ActivityStatusViewModel = viewModel(
        factory = viewModelFactory { initializer { ActivityStatusViewModel(FitnessContainer.activityRepository) } }
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    when {
        state.loading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator(color = MovaOrange) }
        state.isEmpty -> NoActivitiesScreen(onAddActivity = onAdd, onBack = onBack)
        else -> ActivityStatusScreen(state) { event ->
            when (event) {
                ActivityStatusEvent.AddClicked -> onAdd()
                is ActivityStatusEvent.ActivityClicked -> onActivityClick(event.logId)
                ActivityStatusEvent.BackClicked -> onBack()
            }
            viewModel.onEvent(event)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ActivityStatusScreen(state: ActivityStatusUiState, onEvent: (ActivityStatusEvent) -> Unit) {
    val legend = listOf(ActivityType.Running, ActivityType.Jogging, ActivityType.Biking, ActivityType.Weightlifting, ActivityType.Yoga)
    LazyColumn(
        modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            TopBarBack(onBack = { onEvent(ActivityStatusEvent.BackClicked) })
            Text(
                "Activity Status", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            FlowRow(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                legend.forEach { t ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(14.dp).clip(CircleShape).background(t.chartColor()))
                        Text(t.label, fontSize = 15.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 6.dp))
                    }
                }
            }
            TiltedTilesChart(
                items = state.hoursByType.map { (type, hours) -> TileItem(hours, type.chartColor(), type.chartTextColor()) }
            )
            MovaPrimaryButton(
                text = "Add New Activity", onClick = { onEvent(ActivityStatusEvent.AddClicked) },
                containerColor = MovaOrange, modifier = Modifier.padding(vertical = 16.dp)
            )
            Text("Recent", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp))
        }
        items(state.recent, key = { it.id }) { log ->
            Row(
                modifier = Modifier
                    .fillMaxWidth().padding(bottom = 10.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { onEvent(ActivityStatusEvent.ActivityClicked(log.id)) }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(log.type.emoji(), fontSize = 28.sp)
                Column(Modifier.weight(1f).padding(start = 14.dp)) {
                    Text(log.type.label, fontWeight = FontWeight.Bold)
                    Text(
                        DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(log.startedAt)),
                        fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text("${log.durationSec / 60} min · ${log.kcal} kcal", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

private val sampleLogs = listOf(
    ActivityLog("1", ActivityType.Running, System.currentTimeMillis(), 3600, 8000.0, 700),
    ActivityLog("2", ActivityType.Yoga, System.currentTimeMillis() - 86_400_000, 1800, 0.0, 90)
)
private val sampleState = ActivityStatusUiState(
    loading = false,
    hoursByType = mapOf(ActivityType.Running to 68.0, ActivityType.Jogging to 1.0, ActivityType.Biking to 7.0,
        ActivityType.Weightlifting to 15.0, ActivityType.Yoga to 87.0),
    recent = sampleLogs
)

@Preview(showBackground = true, heightDp = 1000)
@Composable
private fun ActivityStatusPreview() {
    MovaTheme(darkTheme = false) { ActivityStatusScreen(sampleState, {}) }
}

@Preview(showBackground = true, heightDp = 1000)
@Composable
private fun ActivityStatusDarkPreview() {
    MovaTheme(darkTheme = true) { ActivityStatusScreen(sampleState, {}) }
}
