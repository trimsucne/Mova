package co.edu.unal.dadm.mova.presentation.viewmodel.home

import co.edu.unal.dadm.mova.domain.model.SearchResult

data class SearchUiState(
    val query: String = "",
    val results: List<SearchResult> = emptyList(),
    val suggestions: List<String> = listOf("Upper Body", "Hydration", "Salad", "Score", "HIIT"),
    val isLoading: Boolean = false
)

