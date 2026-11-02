package co.edu.unal.dadm.mova.presentation.screen.nutrition

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.unal.dadm.mova.core.di.NutritionContainer
import co.edu.unal.dadm.mova.presentation.components.MovaPrimaryButton
import co.edu.unal.dadm.mova.presentation.components.MovaTextField
import co.edu.unal.dadm.mova.presentation.components.TopBarBack
import co.edu.unal.dadm.mova.presentation.screen.nutrition.components.MealImage
import co.edu.unal.dadm.mova.presentation.screen.nutrition.components.MealTypeChips
import co.edu.unal.dadm.mova.presentation.theme.MovaMuted
import co.edu.unal.dadm.mova.presentation.viewmodel.nutrition.MealDetailEvent
import co.edu.unal.dadm.mova.presentation.viewmodel.nutrition.MealDetailViewModel
import co.edu.unal.dadm.mova.presentation.viewmodel.nutrition.NutritionViewModelFactory
import java.time.LocalDate
import kotlin.math.roundToInt

@Composable
fun MealDetailScreen(
    container: NutritionContainer,
    mealId: String,
    date: LocalDate,
    onBack: () -> Unit,
    onAdded: (LocalDate) -> Unit
) {
    val viewModel: MealDetailViewModel = viewModel(factory = NutritionViewModelFactory {
        MealDetailViewModel(mealId, date, LocalDate.now(), container.mealRepository, container.logMeal, container.currentUserId)
    })
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.addedOn) { state.addedOn?.let(onAdded) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TopBarBack(onBack = onBack, title = "Meal Detail")
        val meal = state.meal
        when {
            state.isLoading -> CircularProgressIndicator()
            meal == null -> Text(state.error ?: "Meal not found")
            else -> {
                MealImage(meal.name, meal.imageRes, Modifier.fillMaxWidth().height(220.dp))
                Text(meal.name, style = MaterialTheme.typography.headlineSmall)
                Text("${meal.category.label} · ${meal.prepMinutes} min", color = MovaMuted)
                var gramsText by rememberSaveable(meal.id) { mutableStateOf(state.grams.toString()) }
                MovaTextField(
                    value = gramsText,
                    onValueChange = { text ->
                        gramsText = text.filter(Char::isDigit)
                        gramsText.toIntOrNull()?.takeIf { it > 0 }?.let { viewModel.onEvent(MealDetailEvent.GramsChanged(it)) }
                    },
                    label = "Portion (g)",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Protein ${state.macros.proteinG.roundToInt()} g")
                    Text("Carbs ${state.macros.carbsG.roundToInt()} g")
                    Text("Fat ${state.macros.fatG.roundToInt()} g")
                }
                Text("${state.kcal} kcal", style = MaterialTheme.typography.titleLarge)
                MealTypeChips(
                    selected = state.mealType,
                    onSelect = { type -> type?.let { viewModel.onEvent(MealDetailEvent.MealTypeChanged(it)) } },
                    includeAll = false
                )
                state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                MovaPrimaryButton(
                    text = if (state.isSaving) "Adding..." else "Add Meal",
                    onClick = { viewModel.onEvent(MealDetailEvent.AddMeal) },
                    enabled = !state.isSaving && (gramsText.toIntOrNull() ?: 0) > 0,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
