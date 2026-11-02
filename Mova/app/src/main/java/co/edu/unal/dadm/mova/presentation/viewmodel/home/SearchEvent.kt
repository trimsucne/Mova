package co.edu.unal.dadm.mova.presentation.viewmodel.home

sealed interface SearchEvent {
    data class QueryChanged(val query: String) : SearchEvent
    data class SelectSuggestion(val suggestion: String) : SearchEvent
}

