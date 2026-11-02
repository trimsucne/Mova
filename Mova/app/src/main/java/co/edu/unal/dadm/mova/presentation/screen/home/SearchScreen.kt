package co.edu.unal.dadm.mova.presentation.screen.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.unal.dadm.mova.core.di.SearchContainer
import co.edu.unal.dadm.mova.domain.model.SearchResult
import co.edu.unal.dadm.mova.presentation.components.MovaTextField
import co.edu.unal.dadm.mova.presentation.components.TopBarBack
import co.edu.unal.dadm.mova.presentation.screen.nutrition.components.MealImage
import co.edu.unal.dadm.mova.presentation.theme.MovaMuted
import co.edu.unal.dadm.mova.presentation.theme.MovaOrange
import co.edu.unal.dadm.mova.presentation.viewmodel.home.SearchEvent
import co.edu.unal.dadm.mova.presentation.viewmodel.home.SearchStatus
import co.edu.unal.dadm.mova.presentation.viewmodel.home.SearchViewModel
import co.edu.unal.dadm.mova.presentation.viewmodel.nutrition.NutritionViewModelFactory

@Composable
fun SearchScreen(
    container: SearchContainer,
    initialQuery: String,
    onBack: () -> Unit,
    onOpenMeal: (String) -> Unit
) {
    val viewModel: SearchViewModel = viewModel(factory = NutritionViewModelFactory {
        SearchViewModel(
            initialQuery, container.workoutsAvailable, container.searchCatalog, container.repository, container.repository,
            container.recentSearches, container.mealRepository, container.logMeal, container.currentUserId
        )
    })
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(state.added) {
        val added = state.added ?: return@LaunchedEffect
        val result = snackbar.showSnackbar(message = "Añadido a ${added.mealType.label}", actionLabel = "Deshacer")
        viewModel.onEvent(if (result == SnackbarResult.ActionPerformed) SearchEvent.UndoAdd else SearchEvent.AddedShown)
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbar) }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TopBarBack(onBack = onBack, title = "Search")
            MovaTextField(
                value = state.query,
                onValueChange = { viewModel.onEvent(SearchEvent.QueryChanged(it)) },
                label = "Search",
                placeholder = "Search our food database...",
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { viewModel.onEvent(SearchEvent.Submit) })
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                state.availableFilters.forEach { filter ->
                    FilterChip(
                        selected = state.filter == filter,
                        onClick = { viewModel.onEvent(SearchEvent.FilterChanged(filter)) },
                        label = { Text(filter.label) }
                    )
                }
            }
            val showSuggestions = state.suggestions.isNotEmpty() && state.suggestions.none { it.equals(state.query.trim(), ignoreCase = true) }
            if (showSuggestions) {
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.suggestions.forEach { suggestion ->
                        AssistChip(onClick = { viewModel.onEvent(SearchEvent.SuggestionClicked(suggestion)) }, label = { Text(suggestion) })
                    }
                }
            }

            when (state.status) {
                SearchStatus.IDLE -> RecentList(
                    recent = state.recent,
                    onClick = { viewModel.onEvent(SearchEvent.RecentClicked(it)) },
                    onClear = { viewModel.onEvent(SearchEvent.ClearRecent) }
                )
                SearchStatus.LOADING -> Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                SearchStatus.RESULTS -> ResultList(
                    results = state.results,
                    onOpen = { result ->
                        viewModel.onEvent(SearchEvent.ResultOpened(result))
                        if (result.type == SearchResult.Type.MEAL) onOpenMeal(result.id)
                    },
                    onAdd = { viewModel.onEvent(SearchEvent.AddMeal(it)) }
                )
                SearchStatus.EMPTY -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("No encontramos resultados para \"${state.query.trim()}\"", style = MaterialTheme.typography.titleMedium)
                    if (state.fallback.isNotEmpty()) {
                        Text("Maybe you are looking for:", color = MovaMuted)
                        ResultList(
                            results = state.fallback,
                            showScore = false,
                            onOpen = { if (it.type == SearchResult.Type.MEAL) onOpenMeal(it.id) },
                            onAdd = { viewModel.onEvent(SearchEvent.AddMeal(it)) }
                        )
                    }
                }
                SearchStatus.ERROR -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(state.error ?: "Search failed", color = MaterialTheme.colorScheme.error)
                    TextButton(onClick = { viewModel.onEvent(SearchEvent.Retry) }) { Text("Retry") }
                }
            }
        }
    }
}

@Composable
private fun RecentList(recent: List<String>, onClick: (String) -> Unit, onClear: () -> Unit) {
    if (recent.isEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Recent searches", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
            TextButton(onClick = onClear) { Text("Clear") }
        }
        recent.forEach { text ->
            Text(text, modifier = Modifier.fillMaxWidth().clickable { onClick(text) }.padding(vertical = 8.dp))
        }
    }
}

@Composable
private fun ResultList(
    results: List<SearchResult>,
    onOpen: (SearchResult) -> Unit,
    onAdd: (SearchResult) -> Unit,
    showScore: Boolean = true
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(results, key = { "${it.type}-${it.id}" }) { result ->
            Card(modifier = Modifier.fillMaxWidth().clickable { onOpen(result) }) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (result.type == SearchResult.Type.MEAL && result.imageRes != null) {
                        MealImage(result.title, result.imageRes, Modifier.size(48.dp))
                    } else {
                        Text(if (result.type == SearchResult.Type.MEAL) "🍽" else "🏋", style = MaterialTheme.typography.headlineSmall)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(result.title, style = MaterialTheme.typography.titleSmall)
                        Text(result.subtitle, color = MovaMuted)
                    }
                    if (showScore) Text("${result.score}% Match", color = MovaOrange)
                    if (result.type == SearchResult.Type.MEAL) {
                        TextButton(onClick = { onAdd(result) }) { Text("+") }
                    }
                }
            }
        }
    }
}
