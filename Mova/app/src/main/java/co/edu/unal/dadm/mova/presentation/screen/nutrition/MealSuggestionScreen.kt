package co.edu.unal.dadm.mova.presentation.screen.nutrition

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.unal.dadm.mova.core.di.NutritionContainer
import co.edu.unal.dadm.mova.domain.model.Meal
import co.edu.unal.dadm.mova.domain.model.MealCategory
import co.edu.unal.dadm.mova.presentation.components.TopBarBack
import co.edu.unal.dadm.mova.presentation.screen.nutrition.components.MealImage
import co.edu.unal.dadm.mova.presentation.theme.MovaMuted
import co.edu.unal.dadm.mova.presentation.viewmodel.nutrition.MealSuggestionEvent
import co.edu.unal.dadm.mova.presentation.viewmodel.nutrition.MealSuggestionViewModel
import co.edu.unal.dadm.mova.presentation.viewmodel.nutrition.NutritionViewModelFactory
import java.time.LocalDate

@Composable
fun MealSuggestionScreen(
    container: NutritionContainer,
    onBack: () -> Unit,
    onMeal: (String) -> Unit
) {
    val viewModel: MealSuggestionViewModel = viewModel(factory = NutritionViewModelFactory {
        MealSuggestionViewModel(
            LocalDate.now(), container.mealRepository, container.profileRepository,
            container.observeDailyIntake, container.suggestMeals, container.currentUserId
        )
    })
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        TopBarBack(onBack = onBack, title = "Explore")
        if (state.isLoading) {
            CircularProgressIndicator()
            return@Column
        }
        if (state.suggestions.isNotEmpty()) {
            Text("Suggested for you", style = MaterialTheme.typography.titleMedium)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(state.suggestions, key = { it.id }) { meal ->
                    Card(modifier = Modifier.width(160.dp).clickable { onMeal(meal.id) }) {
                        MealImage(meal.name, meal.imageRes, Modifier.fillMaxWidth().height(100.dp))
                        Text(meal.name, modifier = Modifier.padding(8.dp), maxLines = 2)
                        Text("${meal.kcal} kcal", color = MovaMuted, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                }
            }
        }
        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = state.category == null, onClick = { viewModel.onEvent(MealSuggestionEvent.SelectCategory(null)) }, label = { Text("All") })
            MealCategory.entries.forEach { category ->
                FilterChip(
                    selected = state.category == category,
                    onClick = { viewModel.onEvent(MealSuggestionEvent.SelectCategory(category)) },
                    label = { Text(category.label) }
                )
            }
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(state.visibleCatalog, key = { it.id }) { meal -> CatalogRow(meal, onClick = { onMeal(meal.id) }) }
        }
    }
}

@Composable
private fun CatalogRow(meal: Meal, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MealImage(meal.name, meal.imageRes, Modifier.width(72.dp).height(72.dp))
            Column {
                Text(meal.name, style = MaterialTheme.typography.titleSmall)
                Text(meal.category.label, color = MovaMuted)
                Text("${meal.kcal} kcal · ${meal.prepMinutes} min", color = MovaMuted)
            }
        }
    }
}
