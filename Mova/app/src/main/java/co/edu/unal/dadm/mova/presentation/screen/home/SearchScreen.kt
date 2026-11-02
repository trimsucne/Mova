package co.edu.unal.dadm.mova.presentation.screen.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.unal.dadm.mova.presentation.components.MovaIcons
import co.edu.unal.dadm.mova.presentation.theme.MovaColor
import co.edu.unal.dadm.mova.presentation.viewmodel.home.SearchEvent
import co.edu.unal.dadm.mova.presentation.viewmodel.home.SearchViewModel

@Composable
fun SearchScreenRoute(
    initialQuery: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: SearchViewModel = viewModel(
        factory = SearchViewModel.Factory(initialQuery)
    )
    val uiState by viewModel.uiState.collectAsState()

    SearchScreen(
        uiState = uiState,
        onQueryChange = { viewModel.onEvent(SearchEvent.QueryChanged(it)) },
        onSuggestionClick = { viewModel.onEvent(SearchEvent.SelectSuggestion(it)) },
        onBack = onBack,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    uiState: co.edu.unal.dadm.mova.presentation.viewmodel.home.SearchUiState,
    onQueryChange: (String) -> Unit,
    onSuggestionClick: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    OutlinedTextField(
                        value = uiState.query,
                        onValueChange = onQueryChange,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search workouts, meals...") },
                        singleLine = true,
                        leadingIcon = {
                            Icon(MovaIcons.Search, contentDescription = null, tint = MovaColor.TextSecondary)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MovaColor.CardLightGray,
                            unfocusedContainerColor = MovaColor.CardLightGray,
                            focusedBorderColor = MovaColor.PrimaryOrange,
                            unfocusedBorderColor = MovaColor.CardBorder
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("←", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MovaColor.BackgroundWhite)
            )
        },
        containerColor = MovaColor.BackgroundWhite,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Popular Suggestions", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MovaColor.TextBlack)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                uiState.suggestions.forEach { suggestion ->
                    SuggestionChip(
                        onClick = { onSuggestionClick(suggestion) },
                        label = { Text(suggestion) },
                        colors = SuggestionChipDefaults.suggestionChipColors(containerColor = MovaColor.CardLightGray)
                    )
                }
            }

            if (uiState.results.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Text("No results found for '${uiState.query}'", color = MovaColor.TextSecondary)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.results) { item ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp)),
                            colors = CardDefaults.cardColors(containerColor = MovaColor.CardLightGray)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(14.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(item.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MovaColor.TextBlack)
                                    Text(item.subtitle, fontSize = 12.sp, color = MovaColor.TextSecondary)
                                }
                                Surface(
                                    color = MovaColor.PrimaryOrangeLight,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        item.category,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MovaColor.PrimaryOrangeDark
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

