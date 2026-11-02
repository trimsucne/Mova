package co.edu.unal.dadm.mova.presentation.viewmodel.home

import co.edu.unal.dadm.mova.domain.model.MealType
import co.edu.unal.dadm.mova.domain.model.SearchResult

enum class SearchFilter(val label: String, val types: Set<SearchResult.Type>) {
    ALL("All", setOf(SearchResult.Type.MEAL, SearchResult.Type.WORKOUT)),
    MEALS("Meals", setOf(SearchResult.Type.MEAL)),
    WORKOUTS("Workouts", setOf(SearchResult.Type.WORKOUT))
}

enum class SearchStatus { IDLE, LOADING, RESULTS, EMPTY, ERROR }

/** Comida añadida desde un resultado (B8); la pantalla muestra el snackbar mientras no sea null. */
data class AddedMeal(val logId: String, val mealType: MealType)

data class SearchUiState(
    val query: String = "",
    val filter: SearchFilter = SearchFilter.ALL,
    val availableFilters: List<SearchFilter> = listOf(SearchFilter.ALL, SearchFilter.MEALS),
    val status: SearchStatus = SearchStatus.IDLE,
    val results: List<SearchResult> = emptyList(),
    /** B6: nombres que empiezan por la consulta. */
    val suggestions: List<String> = emptyList(),
    /** Sugerencias del estado «Not Found». */
    val fallback: List<SearchResult> = emptyList(),
    /** B7: se muestran cuando el campo está vacío. */
    val recent: List<String> = emptyList(),
    val added: AddedMeal? = null,
    val error: String? = null
)
