package co.edu.unal.dadm.mova.presentation.screen.nutrition

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.unal.dadm.mova.core.di.NutritionContainer
import co.edu.unal.dadm.mova.presentation.components.TopBarBack
import co.edu.unal.dadm.mova.presentation.theme.MovaBlueIcon
import co.edu.unal.dadm.mova.presentation.theme.MovaMuted
import co.edu.unal.dadm.mova.presentation.theme.MovaOrange
import co.edu.unal.dadm.mova.presentation.theme.MovaPurpleIcon
import co.edu.unal.dadm.mova.presentation.viewmodel.nutrition.CalorieStatsViewModel
import co.edu.unal.dadm.mova.presentation.viewmodel.nutrition.NutritionViewModelFactory
import java.time.LocalDate

@Composable
fun CalorieStatsScreen(container: NutritionContainer, onBack: () -> Unit) {
    val viewModel: CalorieStatsViewModel = viewModel(factory = NutritionViewModelFactory {
        CalorieStatsViewModel(LocalDate.now(), container.mealRepository, container.currentUserId)
    })
    val state by viewModel.state.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            TopBarBack(onBack = onBack, title = "Calorie Stats")
            val share = state.share
            when {
                state.isLoading -> CircularProgressIndicator()
                share == null -> Text("No meals logged this month yet.", color = MovaMuted)
                else -> {
                    Text("${state.totalKcal} kcal this month", style = MaterialTheme.typography.titleLarge)
                    ShareBar("Protein", share.proteinPct, MovaOrange)
                    ShareBar("Carbs", share.carbsPct, MovaBlueIcon)
                    ShareBar("Fat", share.fatPct, MovaPurpleIcon)
                }
            }
        }
        // Insignia visual (D6): N9 no se bloquea en el prototipo.
        Text(
            text = "PREMIUM",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(20.dp)
                .background(MovaOrange, RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun ShareBar(label: String, percent: Int, color: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(label, modifier = Modifier.weight(1f))
            Text("$percent %")
        }
        LinearProgressIndicator(progress = { percent / 100f }, color = color, modifier = Modifier.fillMaxWidth().height(10.dp))
    }
}
