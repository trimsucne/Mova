package co.edu.unal.dadm.mova.presentation.screen.nutrition

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.unal.dadm.mova.core.di.NutritionContainer
import co.edu.unal.dadm.mova.domain.model.MealType
import co.edu.unal.dadm.mova.presentation.components.MovaPrimaryButton
import co.edu.unal.dadm.mova.presentation.components.TopBarBack
import co.edu.unal.dadm.mova.presentation.screen.nutrition.components.MealTypeChips
import co.edu.unal.dadm.mova.presentation.theme.MovaMuted
import co.edu.unal.dadm.mova.presentation.viewmodel.nutrition.MyMealsEvent
import co.edu.unal.dadm.mova.presentation.viewmodel.nutrition.MyMealsViewModel
import co.edu.unal.dadm.mova.presentation.viewmodel.nutrition.NutritionViewModelFactory
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun MyMealsScreen(
    container: NutritionContainer,
    initialDate: LocalDate,
    onBack: () -> Unit,
    onAddMeal: (MealType?, LocalDate) -> Unit,
    onExplore: () -> Unit
) {
    val viewModel: MyMealsViewModel = viewModel(factory = NutritionViewModelFactory {
        MyMealsViewModel(initialDate, LocalDate.now(), container.mealRepository, container.currentUserId)
    })
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        TopBarBack(onBack = onBack, title = "My Meals")
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(state.days) { day ->
                FilterChip(
                    selected = day == state.selectedDate,
                    onClick = { viewModel.onEvent(MyMealsEvent.SelectDate(day)) },
                    label = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(day.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()))
                            Text(day.dayOfMonth.toString())
                        }
                    }
                )
            }
        }
        MealTypeChips(selected = state.filter, onSelect = { viewModel.onEvent(MyMealsEvent.SelectFilter(it)) }, includeAll = true)
        Text("${state.totalKcal} kcal", style = MaterialTheme.typography.titleMedium)
        state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        if (state.visibleLogs.isEmpty()) {
            Text("No meals logged for this day yet.", color = MovaMuted, modifier = Modifier.weight(1f))
        } else {
            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.visibleLogs, key = { it.id }) { log ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(log.name, style = MaterialTheme.typography.titleSmall)
                                Text(
                                    "${log.mealType.label} · P ${log.macros.proteinG.roundToInt()} g · C ${log.macros.carbsG.roundToInt()} g · F ${log.macros.fatG.roundToInt()} g",
                                    color = MovaMuted
                                )
                            }
                            Text("${log.kcal} kcal")
                            TextButton(onClick = { viewModel.onEvent(MyMealsEvent.RequestDelete(log)) }) { Text("Delete") }
                        }
                    }
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MovaPrimaryButton(text = "Add Meal", onClick = { onAddMeal(state.filter, state.selectedDate) }, modifier = Modifier.weight(1f))
            MovaPrimaryButton(text = "Explore", onClick = onExplore, modifier = Modifier.weight(1f), trailingText = null)
        }
    }

    state.pendingDelete?.let { log ->
        AlertDialog(
            onDismissRequest = { viewModel.onEvent(MyMealsEvent.CancelDelete) },
            title = { Text("Delete meal?") },
            text = { Text("${log.name} will be removed from ${log.date}.") },
            confirmButton = { TextButton(onClick = { viewModel.onEvent(MyMealsEvent.ConfirmDelete) }) { Text("Delete") } },
            dismissButton = { TextButton(onClick = { viewModel.onEvent(MyMealsEvent.CancelDelete) }) { Text("Cancel") } }
        )
    }
}
