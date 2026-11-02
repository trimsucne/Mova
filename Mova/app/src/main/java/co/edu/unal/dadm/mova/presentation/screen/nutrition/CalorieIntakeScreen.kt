package co.edu.unal.dadm.mova.presentation.screen.nutrition

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.unal.dadm.mova.core.di.NutritionContainer
import co.edu.unal.dadm.mova.presentation.charts.SmoothAreaChart
import co.edu.unal.dadm.mova.presentation.components.MovaPrimaryButton
import co.edu.unal.dadm.mova.presentation.components.TopBarBack
import co.edu.unal.dadm.mova.presentation.screen.nutrition.components.MacroBar
import co.edu.unal.dadm.mova.presentation.theme.MovaBlueIcon
import co.edu.unal.dadm.mova.presentation.theme.MovaMuted
import co.edu.unal.dadm.mova.presentation.theme.MovaOrange
import co.edu.unal.dadm.mova.presentation.theme.MovaPurpleIcon
import co.edu.unal.dadm.mova.presentation.viewmodel.nutrition.CalorieIntakeViewModel
import co.edu.unal.dadm.mova.presentation.viewmodel.nutrition.NutritionViewModelFactory
import java.time.LocalDate

@Composable
fun CalorieIntakeScreen(
    container: NutritionContainer,
    onBack: () -> Unit,
    onMyMeals: () -> Unit,
    onAddMeal: () -> Unit,
    onExplore: () -> Unit,
    onStats: () -> Unit
) {
    val viewModel: CalorieIntakeViewModel = viewModel(factory = NutritionViewModelFactory {
        CalorieIntakeViewModel(LocalDate.now(), container.observeDailyIntake, container.mealRepository, container.currentUserId)
    })
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TopBarBack(onBack = onBack, title = "Calorie Intake")
        val intake = state.intake
        if (intake == null) {
            CircularProgressIndicator()
        } else {
            Text("${intake.consumedKcal} kcal", style = MaterialTheme.typography.displaySmall)
            val exceeded = state.exceededBy
            Text(
                text = if (exceeded != null) "Hoy superaste tu meta por $exceeded kcal; mañana se equilibra"
                else "${intake.remainingKcal} kcal left of ${intake.goalKcal}",
                color = MovaMuted
            )
            if (intake.burnedKcal > 0) {
                Text("Goal includes ${intake.burnedKcal} kcal burned today", color = MovaMuted)
            }
            MacroBar("Protein", intake.consumed.proteinG, intake.goalMacros.proteinG, MovaOrange)
            MacroBar("Carbs", intake.consumed.carbsG, intake.goalMacros.carbsG, MovaBlueIcon)
            MacroBar("Fat", intake.consumed.fatG, intake.goalMacros.fatG, MovaPurpleIcon)
        }

        Text("This month", style = MaterialTheme.typography.titleMedium)
        SmoothAreaChart(
            values = state.monthlyKcal.map { it.toFloat() },
            modifier = Modifier.fillMaxWidth().height(160.dp)
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MovaPrimaryButton(text = "My Meals", onClick = onMyMeals, modifier = Modifier.weight(1f), trailingText = null)
            MovaPrimaryButton(text = "Add Meal", onClick = onAddMeal, modifier = Modifier.weight(1f), trailingText = null)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MovaPrimaryButton(text = "Explore", onClick = onExplore, modifier = Modifier.weight(1f), trailingText = null)
            MovaPrimaryButton(text = "Stats", onClick = onStats, modifier = Modifier.weight(1f), trailingText = null)
        }
    }
}
