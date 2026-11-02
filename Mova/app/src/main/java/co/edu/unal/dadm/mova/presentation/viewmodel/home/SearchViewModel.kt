package co.edu.unal.dadm.mova.presentation.viewmodel.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.unal.dadm.mova.core.common.Result
import co.edu.unal.dadm.mova.domain.model.MealLog
import co.edu.unal.dadm.mova.domain.model.MealType
import co.edu.unal.dadm.mova.domain.model.SearchResult
import co.edu.unal.dadm.mova.domain.model.SearchScorer
import co.edu.unal.dadm.mova.domain.repository.MealRepository
import co.edu.unal.dadm.mova.domain.repository.RecentSearchesStore
import co.edu.unal.dadm.mova.domain.repository.SearchFallbacks
import co.edu.unal.dadm.mova.domain.repository.SearchRepository
import co.edu.unal.dadm.mova.domain.usecase.LogMealUseCase
import co.edu.unal.dadm.mova.domain.usecase.SearchCatalogUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class SearchViewModel(
    initialQuery: String,
    workoutsAvailable: Boolean,
    private val searchCatalog: SearchCatalogUseCase,
    private val searchRepository: SearchRepository,
    private val fallbacks: SearchFallbacks,
    private val recentSearches: RecentSearchesStore,
    private val mealRepository: MealRepository,
    private val logMeal: LogMealUseCase,
    private val currentUserId: () -> String,
    private val today: () -> LocalDate = LocalDate::now,
    private val clock: () -> Instant = Instant::now
) : ViewModel() {

    private val query = MutableStateFlow(initialQuery)
    private val filter = MutableStateFlow(SearchFilter.ALL)
    private val retries = MutableStateFlow(0)
    private var addInFlight = false

    private val _state = MutableStateFlow(
        SearchUiState(
            query = initialQuery,
            availableFilters = if (workoutsAvailable) SearchFilter.entries else listOf(SearchFilter.ALL, SearchFilter.MEALS)
        )
    )
    val state: StateFlow<SearchUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            recentSearches.recent.collect { recent -> _state.update { it.copy(recent = recent) } }
        }
        viewModelScope.launch {
            combine(query, filter, retries) { q, f, _ -> q to f }
                .onEach { (q, _) ->
                    val searchable = isSearchable(q)
                    _state.update {
                        if (searchable) it.copy(status = SearchStatus.LOADING, error = null)
                        else it.copy(status = SearchStatus.IDLE, results = emptyList(), error = null)
                    }
                }
                .debounce(DEBOUNCE_MS)
                .flatMapLatest { (q, f) -> if (isSearchable(q)) outcomes(q, f) else flowOf(null) }
                .collect { outcome -> outcome?.let(::applyOutcome) }
        }
        viewModelScope.launch {
            query.debounce(DEBOUNCE_MS)
                .flatMapLatest { q ->
                    if (isSearchable(q)) searchRepository.suggestions(q).catch { emit(emptyList()) } else flowOf(emptyList())
                }
                .collect { suggestions -> _state.update { it.copy(suggestions = suggestions) } }
        }
        viewModelScope.launch {
            filter.flatMapLatest { f -> fallbacks.firstItems(f.types, FALLBACK_COUNT).catch { emit(emptyList()) } }
                .collect { items -> _state.update { it.copy(fallback = items) } }
        }
    }

    fun onEvent(event: SearchEvent) {
        when (event) {
            is SearchEvent.QueryChanged -> setQuery(event.query)
            is SearchEvent.FilterChanged -> {
                filter.value = event.filter
                _state.update { it.copy(filter = event.filter) }
            }
            SearchEvent.Submit -> saveRecent(_state.value.query)
            SearchEvent.Retry -> retries.update { it + 1 }
            is SearchEvent.SuggestionClicked -> setQuery(event.text)
            is SearchEvent.RecentClicked -> setQuery(event.text)
            SearchEvent.ClearRecent -> viewModelScope.launch { recentSearches.clear() }
            is SearchEvent.ResultOpened -> saveRecent(_state.value.query)
            is SearchEvent.AddMeal -> addMeal(event.result)
            SearchEvent.UndoAdd -> undoAdd()
            SearchEvent.AddedShown -> _state.update { it.copy(added = null) }
        }
    }

    private fun setQuery(text: String) {
        query.value = text
        _state.update { it.copy(query = text) }
    }

    private fun saveRecent(text: String) {
        if (isSearchable(text)) viewModelScope.launch { recentSearches.add(text) }
    }

    private sealed interface Outcome {
        data class Found(val results: List<SearchResult>) : Outcome
        data class Failed(val message: String) : Outcome
    }

    private fun outcomes(q: String, f: SearchFilter): Flow<Outcome> =
        searchCatalog(q, f.types)
            .map<List<SearchResult>, Outcome> { Outcome.Found(it) }
            .catch { emit(Outcome.Failed(it.message ?: "Search failed")) }

    private fun applyOutcome(outcome: Outcome) {
        _state.update {
            when (outcome) {
                is Outcome.Found -> it.copy(
                    results = outcome.results,
                    status = if (outcome.results.isEmpty()) SearchStatus.EMPTY else SearchStatus.RESULTS
                )
                is Outcome.Failed -> it.copy(results = emptyList(), status = SearchStatus.ERROR, error = outcome.message)
            }
        }
    }

    /** B8: porción base, hoy, primer tipo de suitableFor en el orden del enum. */
    private fun addMeal(result: SearchResult) {
        if (result.type != SearchResult.Type.MEAL) return
        if (addInFlight) return
        addInFlight = true
        viewModelScope.launch {
            try {
                logAdded(result)
            } finally {
                addInFlight = false
            }
        }
    }

    private suspend fun logAdded(result: SearchResult) {
        val meal = mealRepository.observeMeal(result.id).first() ?: return
        val mealType = meal.suitableFor.minOrNull() ?: MealType.SNACK
        val entry = MealLog.fromMeal(meal, meal.servingGrams, currentUserId(), today(), mealType, clock())
        when (val logged = logMeal(entry)) {
            is Result.Success -> _state.update { it.copy(added = AddedMeal(logged.data, mealType)) }
            is Result.Error -> _state.update { it.copy(error = logged.message) }
            Result.Loading -> Unit
        }
    }

    private fun undoAdd() {
        val added = _state.value.added ?: return
        _state.update { it.copy(added = null) }
        viewModelScope.launch { mealRepository.delete(added.logId) }
    }

    private fun isSearchable(text: String) = SearchScorer.normalize(text).length >= MIN_QUERY_LENGTH

    private companion object {
        const val DEBOUNCE_MS = 300L
        const val MIN_QUERY_LENGTH = 2
        const val FALLBACK_COUNT = 3
    }
}
