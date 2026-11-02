package co.edu.unal.dadm.mova.presentation.viewmodel.nutrition

import co.edu.unal.dadm.mova.domain.model.Meal
import co.edu.unal.dadm.mova.domain.model.MealCategory

data class MealSuggestionUiState(
    val catalog: List<Meal> = emptyList(),
    val category: MealCategory? = null,
    val suggestions: List<Meal> = emptyList(),
    val isLoading: Boolean = true
) {
    val visibleCatalog: List<Meal> get() = category?.let { selected -> catalog.filter { it.category == selected } } ?: catalog
}
