package co.edu.unal.dadm.mova.presentation.screen.nutrition

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.unal.dadm.mova.core.di.NutritionContainer
import co.edu.unal.dadm.mova.domain.model.MealType
import co.edu.unal.dadm.mova.presentation.components.MovaPrimaryButton
import co.edu.unal.dadm.mova.presentation.components.MovaTextField
import co.edu.unal.dadm.mova.presentation.components.TopBarBack
import co.edu.unal.dadm.mova.presentation.screen.nutrition.components.MealTypeChips
import co.edu.unal.dadm.mova.presentation.viewmodel.nutrition.AddMealEvent
import co.edu.unal.dadm.mova.presentation.viewmodel.nutrition.AddMealViewModel
import co.edu.unal.dadm.mova.presentation.viewmodel.nutrition.NutritionViewModelFactory
import java.time.LocalDate

@Composable
fun AddMealScreen(
    container: NutritionContainer,
    date: LocalDate,
    initialType: MealType?,
    onBack: () -> Unit,
    onAdded: (LocalDate) -> Unit
) {
    val viewModel: AddMealViewModel = viewModel(factory = NutritionViewModelFactory {
        AddMealViewModel(date, LocalDate.now(), initialType, container.logMeal, container.calculateKcal, container.currentUserId)
    })
    val state by viewModel.state.collectAsStateWithLifecycle()
    val number = KeyboardOptions(keyboardType = KeyboardType.Decimal)

    LaunchedEffect(state.addedOn) { state.addedOn?.let(onAdded) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        TopBarBack(onBack = onBack, title = "Add New Meal")
        MovaTextField(
            value = state.name,
            onValueChange = { viewModel.onEvent(AddMealEvent.NameChanged(it)) },
            label = "Meal Name",
            isError = state.nameError != null,
            errorMessage = state.nameError
        )
        MealTypeChips(
            selected = state.mealType,
            onSelect = { type -> type?.let { viewModel.onEvent(AddMealEvent.MealTypeChanged(it)) } },
            includeAll = false
        )
        MovaTextField(value = state.protein, onValueChange = { viewModel.onEvent(AddMealEvent.ProteinChanged(it)) }, label = "Total Protein (g)", keyboardOptions = number)
        MovaTextField(value = state.carbs, onValueChange = { viewModel.onEvent(AddMealEvent.CarbsChanged(it)) }, label = "Total Carbs (g)", keyboardOptions = number)
        MovaTextField(value = state.fat, onValueChange = { viewModel.onEvent(AddMealEvent.FatChanged(it)) }, label = "Total Fat (g)", keyboardOptions = number)
        state.macroError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Text("${state.kcal} kcal", style = MaterialTheme.typography.titleLarge)
        state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        MovaPrimaryButton(
            text = if (state.isSaving) "Adding..." else "Add Meal",
            onClick = { viewModel.onEvent(AddMealEvent.Save) },
            enabled = !state.isSaving,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
