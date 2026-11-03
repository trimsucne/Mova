package co.edu.unal.dadm.mova.presentation.viewmodel.home

import co.edu.unal.dadm.mova.domain.model.SearchResult

sealed interface SearchEvent {
    data class QueryChanged(val query: String) : SearchEvent
    data class FilterChanged(val filter: SearchFilter) : SearchEvent
    data object Submit : SearchEvent
    data object Retry : SearchEvent
    data class SuggestionClicked(val text: String) : SearchEvent
    data class RecentClicked(val text: String) : SearchEvent
    data object ClearRecent : SearchEvent
    data class ResultOpened(val result: SearchResult) : SearchEvent
    data class AddMeal(val result: SearchResult) : SearchEvent
    data object UndoAdd : SearchEvent
    data object AddedShown : SearchEvent
}
