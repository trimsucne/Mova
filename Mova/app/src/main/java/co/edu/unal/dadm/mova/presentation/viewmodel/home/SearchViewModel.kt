package co.edu.unal.dadm.mova.presentation.viewmodel.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import co.edu.unal.dadm.mova.core.di.RepositoryModule
import co.edu.unal.dadm.mova.domain.repository.SearchRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SearchViewModel(
    private val searchRepository: SearchRepository,
    initialQuery: String = ""
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState(query = initialQuery))
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    init {
        if (initialQuery.isNotBlank()) {
            performSearch(initialQuery)
        }
    }

    fun onEvent(event: SearchEvent) {
        when (event) {
            is SearchEvent.QueryChanged -> {
                performSearch(event.query)
            }
            is SearchEvent.SelectSuggestion -> {
                performSearch(event.suggestion)
            }
        }
    }

    private fun performSearch(query: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(query = query, isLoading = true)
            searchRepository.search(query).collect { results ->
                _uiState.value = _uiState.value.copy(results = results, isLoading = false)
            }
        }
    }

    class Factory(private val initialQuery: String) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SearchViewModel(
                searchRepository = RepositoryModule.searchRepository,
                initialQuery = initialQuery
            ) as T
        }
    }
}

