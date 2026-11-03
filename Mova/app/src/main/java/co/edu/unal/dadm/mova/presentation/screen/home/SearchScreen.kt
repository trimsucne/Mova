package co.edu.unal.dadm.mova.presentation.screen.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.unal.dadm.mova.core.di.SearchContainer
import co.edu.unal.dadm.mova.domain.model.MealType
import co.edu.unal.dadm.mova.domain.model.SearchResult
import co.edu.unal.dadm.mova.presentation.screen.nutrition.components.MealImage
import co.edu.unal.dadm.mova.presentation.screen.nutrition.components.LightStatusBarIcons
import co.edu.unal.dadm.mova.presentation.screen.nutrition.components.RoundedSquareBackButton
import co.edu.unal.dadm.mova.presentation.theme.MovaBlueIcon
import co.edu.unal.dadm.mova.presentation.theme.MovaField
import co.edu.unal.dadm.mova.presentation.theme.MovaInk
import co.edu.unal.dadm.mova.presentation.theme.MovaMuted
import co.edu.unal.dadm.mova.presentation.theme.MovaOrange
import co.edu.unal.dadm.mova.presentation.theme.MovaTheme
import co.edu.unal.dadm.mova.presentation.viewmodel.home.AddedMeal
import co.edu.unal.dadm.mova.presentation.viewmodel.home.SearchEvent
import co.edu.unal.dadm.mova.presentation.viewmodel.home.SearchFilter
import co.edu.unal.dadm.mova.presentation.viewmodel.home.SearchStatus
import co.edu.unal.dadm.mova.presentation.viewmodel.home.SearchUiState
import co.edu.unal.dadm.mova.presentation.viewmodel.home.SearchViewModel
import co.edu.unal.dadm.mova.presentation.viewmodel.nutrition.NutritionViewModelFactory

private val ScreenBg = Color(0xFFF8F8F8)
private val RowGrey = Color(0xFFEEEEEE)
private val DarkGrey = Color(0xFF3A3B3F)

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
        val result = snackbar.showSnackbar(message = "Añadido a ${spanishName(added.mealType)}", actionLabel = "Deshacer")
        viewModel.onEvent(if (result == SnackbarResult.ActionPerformed) SearchEvent.UndoAdd else SearchEvent.AddedShown)
    }

    SearchContent(
        state = state,
        snackbar = snackbar,
        onEvent = viewModel::onEvent,
        onBack = onBack,
        onOpenMeal = onOpenMeal
    )
}

@Composable
private fun SearchContent(
    state: SearchUiState,
    snackbar: SnackbarHostState,
    onEvent: (SearchEvent) -> Unit,
    onBack: () -> Unit,
    onOpenMeal: (String) -> Unit
) {
    Scaffold(containerColor = ScreenBg, contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0), snackbarHost = { SnackbarHost(snackbar, modifier = Modifier.navigationBarsPadding().imePadding()) }) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            SearchHeader(state = state, onEvent = onEvent, onBack = onBack)
            Column(
                modifier = Modifier.fillMaxSize().navigationBarsPadding().padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val showSuggestions = state.suggestions.isNotEmpty() && state.suggestions.none { it.equals(state.query.trim(), ignoreCase = true) }
                if (showSuggestions) {
                    SuggestionsCard(
                        suggestions = state.suggestions,
                        onClick = { onEvent(SearchEvent.SuggestionClicked(it)) },
                        modifier = Modifier.offset(y = (-24).dp).zIndex(1f)
                    )
                }
                when (state.status) {
                    SearchStatus.IDLE -> if (state.query.isBlank()) RecentList(
                        recent = state.recent,
                        onClick = { onEvent(SearchEvent.RecentClicked(it)) },
                        onClear = { onEvent(SearchEvent.ClearRecent) }
                    )
                    SearchStatus.LOADING -> LoadingCluster()
                    SearchStatus.RESULTS -> ResultList(
                        results = state.results,
                        onOpen = { result ->
                            onEvent(SearchEvent.ResultOpened(result))
                            if (result.type == SearchResult.Type.MEAL) onOpenMeal(result.id)
                        },
                        onAdd = { onEvent(SearchEvent.AddMeal(it)) }
                    )
                    SearchStatus.EMPTY -> NotFound(
                        query = state.query.trim(),
                        fallback = state.fallback,
                        onOpen = { if (it.type == SearchResult.Type.MEAL) onOpenMeal(it.id) },
                        onAdd = { onEvent(SearchEvent.AddMeal(it)) }
                    )
                    SearchStatus.ERROR -> Column(
                        modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(state.error ?: "Search failed", color = MovaMuted, fontWeight = FontWeight.SemiBold)
                        Text(
                            "Retry",
                            color = MovaOrange,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onEvent(SearchEvent.Retry) }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchHeader(state: SearchUiState, onEvent: (SearchEvent) -> Unit, onBack: () -> Unit) {
    LightStatusBarIcons()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 36.dp, bottomEnd = 36.dp))
            .background(MovaOrange)
            .statusBarsPadding()
            .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            RoundedSquareBackButton(onClick = onBack, onDark = true)
            Text("Search", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White)
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                if (state.query.isEmpty()) {
                    Text("Search our food database...", color = MovaMuted, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
                BasicTextField(
                    value = state.query,
                    onValueChange = { onEvent(SearchEvent.QueryChanged(it)) },
                    singleLine = true,
                    textStyle = TextStyle(color = DarkGrey, fontSize = 16.sp, fontWeight = FontWeight.Bold),
                    cursorBrush = SolidColor(MovaOrange),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { onEvent(SearchEvent.Submit) }),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Text("🔍", fontSize = 20.sp)
        }
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            state.availableFilters.forEach { filter ->
                SearchChip(
                    label = filter.label,
                    icon = when (filter) {
                        SearchFilter.ALL -> null
                        SearchFilter.MEALS -> "🍽"
                        SearchFilter.WORKOUTS -> "🏋"
                    },
                    selected = state.filter == filter,
                    onClick = { onEvent(SearchEvent.FilterChanged(filter)) }
                )
            }
        }
    }
}

@Composable
private fun SearchChip(label: String, icon: String?, selected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(50)
    Row(
        modifier = Modifier
            .clip(shape)
            .background(if (selected) Color.White else Color.Transparent)
            .border(BorderStroke(1.5.dp, Color.White), shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (icon != null) Text(icon, fontSize = 14.sp)
        Text(label, color = if (selected) MovaOrange else Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SuggestionsCard(suggestions: List<String>, onClick: (String) -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(8.dp)
    ) {
        suggestions.forEachIndexed { index, suggestion ->
            Text(
                suggestion,
                color = MovaMuted,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (index == 0) MovaField else Color.Transparent)
                    .clickable { onClick(suggestion) }
                    .padding(horizontal = 12.dp, vertical = 14.dp)
            )
        }
    }
}

@Composable
private fun LoadingCluster() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 64.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Box(modifier = Modifier.size(width = 130.dp, height = 90.dp)) {
            listOf(
                Triple(Color(0xFFEF4444), 0.dp, 0.dp),
                Triple(Color(0xFFF5C518), 40.dp, 0.dp),
                Triple(Color(0xFF14B8A6), 20.dp, 34.dp),
                Triple(Color(0xFF8B37FF), 62.dp, 34.dp)
            ).forEach { (color, x, y) ->
                Box(Modifier.offset(x, y).size(56.dp).clip(CircleShape).background(color.copy(alpha = 0.55f)))
            }
        }
        Text("Loading...", color = MovaMuted, fontSize = 28.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun NotFound(query: String, fallback: List<SearchResult>, onOpen: (SearchResult) -> Unit, onAdd: (SearchResult) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text("🔍", fontSize = 64.sp)
            Text("Not Found", color = MovaInk, fontSize = 34.sp, fontWeight = FontWeight.ExtraBold)
            Text(
                "No encontramos resultados para \"$query\"",
                color = MovaMuted,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold
            )
        }
        if (fallback.isNotEmpty()) {
            Text(
                "Maybe you are looking for:",
                color = MovaInk,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
            ResultList(results = fallback, showScore = false, onOpen = onOpen, onAdd = onAdd)
        }
    }
}

private fun spanishName(type: MealType): String = when (type) {
    MealType.BREAKFAST -> "Desayuno"
    MealType.LUNCH -> "Almuerzo"
    MealType.DINNER -> "Cena"
    MealType.SNACK -> "Snack"
}

@Composable
private fun RecentList(recent: List<String>, onClick: (String) -> Unit, onClear: () -> Unit) {
    if (recent.isEmpty()) return
    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color.White).padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 4.dp)) {
            Text("Recent searches", color = MovaInk, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(1f))
            Text(
                "Clear",
                color = MovaOrange,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(onClick = onClear)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            )
        }
        recent.forEach { text ->
            Text(
                text,
                color = MovaMuted,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onClick(text) }
                    .padding(horizontal = 12.dp, vertical = 14.dp)
            )
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
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(results, key = { "${it.type}-${it.id}" }) { result ->
            val isMeal = result.type == SearchResult.Type.MEAL
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(RowGrey)
                    .clickable { onOpen(result) }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (isMeal && result.imageRes != null) {
                    MealImage(result.title, result.imageRes, Modifier.size(56.dp).clip(RoundedCornerShape(16.dp)))
                } else {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isMeal) MovaOrange else MovaBlueIcon),
                        contentAlignment = Alignment.Center
                    ) { Text(if (isMeal) "🍽" else "🏋", fontSize = 24.sp) }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(result.title, color = MovaInk, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    if (showScore) {
                        Text("${result.score}% Match", color = MovaMuted, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    } else if (result.subtitle.isNotBlank()) {
                        Text(result.subtitle, color = MovaMuted, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
                if (isMeal) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MovaField)
                            .clickable { onAdd(result) },
                        contentAlignment = Alignment.Center
                    ) { Text("+", color = MovaInk, fontSize = 22.sp, fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}

private val sampleResults = listOf(
    SearchResult("m1", SearchResult.Type.MEAL, "Avocado Salad", "Salad & Vegetables", 96, null),
    SearchResult("m2", SearchResult.Type.MEAL, "Avocado Toast", "Breakfast", 88, null),
    SearchResult("w1", SearchResult.Type.WORKOUT, "Avocado Burn HIIT", "20 min", 61, null)
)

@Composable
private fun PreviewHost(state: SearchUiState) {
    MovaTheme {
        SearchContent(state = state, snackbar = remember { SnackbarHostState() }, onEvent = {}, onBack = {}, onOpenMeal = {})
    }
}

@Preview(showBackground = true, heightDp = 780)
@Composable
private fun SearchResultsPreview() = PreviewHost(
    SearchUiState(query = "avocado", status = SearchStatus.RESULTS, results = sampleResults)
)

@Preview(showBackground = true, heightDp = 780)
@Composable
private fun SearchLoadingPreview() = PreviewHost(SearchUiState(query = "avocado", status = SearchStatus.LOADING))

@Preview(showBackground = true, heightDp = 780)
@Composable
private fun SearchNotFoundPreview() = PreviewHost(
    SearchUiState(query = "xyzzy", status = SearchStatus.EMPTY, fallback = sampleResults.take(2))
)

@Preview(showBackground = true, heightDp = 780)
@Composable
private fun SearchSuggestionsPreview() = PreviewHost(
    SearchUiState(query = "av", status = SearchStatus.IDLE, suggestions = listOf("Avocado Salad", "Avocado Toast", "Avocado Dish"))
)

@Preview(showBackground = true, heightDp = 780)
@Composable
private fun SearchRecentsPreview() = PreviewHost(
    SearchUiState(recent = listOf("Salad", "Chicken", "Oatmeal"), added = AddedMeal("l1", MealType.LUNCH))
)
