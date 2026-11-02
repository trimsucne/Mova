package co.edu.unal.dadm.mova.presentation.screen.nutrition

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.unal.dadm.mova.core.di.NutritionContainer
import co.edu.unal.dadm.mova.domain.model.Macros
import co.edu.unal.dadm.mova.domain.model.Meal
import co.edu.unal.dadm.mova.domain.model.MealCategory
import co.edu.unal.dadm.mova.domain.model.MealType
import co.edu.unal.dadm.mova.presentation.screen.nutrition.components.MealImage
import co.edu.unal.dadm.mova.presentation.screen.nutrition.components.PrimaryArrowButton
import co.edu.unal.dadm.mova.presentation.screen.nutrition.components.RoundedSquareBackButton
import co.edu.unal.dadm.mova.presentation.theme.MovaField
import co.edu.unal.dadm.mova.presentation.theme.MovaInk
import co.edu.unal.dadm.mova.presentation.theme.MovaMuted
import co.edu.unal.dadm.mova.presentation.theme.MovaOrange
import co.edu.unal.dadm.mova.presentation.theme.MovaOrangeSoft
import co.edu.unal.dadm.mova.presentation.viewmodel.nutrition.MealSuggestionEvent
import co.edu.unal.dadm.mova.presentation.viewmodel.nutrition.MealSuggestionUiState
import co.edu.unal.dadm.mova.presentation.viewmodel.nutrition.MealSuggestionViewModel
import co.edu.unal.dadm.mova.presentation.viewmodel.nutrition.NutritionViewModelFactory
import kotlinx.coroutines.launch
import java.time.LocalDate

private val ScreenBg = Color(0xFFF8F8F8)

@Composable
fun MealSuggestionScreen(
    container: NutritionContainer,
    onBack: () -> Unit,
    onMeal: (String) -> Unit
) {
    val viewModel: MealSuggestionViewModel = viewModel(factory = NutritionViewModelFactory {
        MealSuggestionViewModel(
            LocalDate.now(), container.mealRepository, container.profileRepository,
            container.observeDailyIntake, container.suggestMeals, container.currentUserId
        )
    })
    val state by viewModel.state.collectAsStateWithLifecycle()
    MealSuggestionContent(
        state = state,
        onBack = onBack,
        onMeal = onMeal,
        onSelectCategory = { viewModel.onEvent(MealSuggestionEvent.SelectCategory(it)) }
    )
}

@Composable
private fun MealSuggestionContent(
    state: MealSuggestionUiState,
    onBack: () -> Unit,
    onMeal: (String) -> Unit,
    onSelectCategory: (MealCategory?) -> Unit
) {
    // Primero las sugerencias, luego el resto del catálogo (sin duplicados), filtrado por categoría.
    val pages = (state.suggestions + state.catalog).distinctBy { it.id }
        .filter { meal -> state.category == null || meal.category == state.category }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBg)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        RoundedSquareBackButton(onClick = onBack)
        if (state.isLoading) {
            Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return@Column
        }
        CategoryRow(selected = state.category, onSelect = onSelectCategory)
        if (pages.isEmpty()) {
            Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Text("No meals found", color = MovaMuted, fontSize = 16.sp)
            }
            return@Column
        }
        MealPager(pages = pages, categoryKey = state.category, onMeal = onMeal, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun CategoryRow(selected: MealCategory?, onSelect: (MealCategory?) -> Unit) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SoftChip("All", selected == null) { onSelect(null) }
        MealCategory.entries.forEach { category ->
            SoftChip(category.label, selected == category) { onSelect(category) }
        }
    }
}

@Composable
private fun SoftChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        text = label,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = if (selected) MovaOrange else MovaMuted,
        modifier = Modifier
            .clip(CircleShape)
            .background(if (selected) MovaOrangeSoft else MovaField)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    )
}

@Composable
private fun MealPager(
    pages: List<Meal>,
    categoryKey: MealCategory?,
    onMeal: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // La clave reinicia el pager al cambiar de categoría.
    key(categoryKey) {
        val pagerState = rememberPagerState { pages.size }
        val scope = rememberCoroutineScope()
        val current = pages[pagerState.currentPage.coerceIn(pages.indices)]

        Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize(), key = { pages[it].id }) { index ->
                    MealPage(pages[index], onClick = { onMeal(pages[index].id) })
                }
                EdgeTab(
                    glyph = "‹",
                    atStart = true,
                    enabled = pagerState.currentPage > 0,
                    onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) } }
                )
                EdgeTab(
                    glyph = "›",
                    atStart = false,
                    enabled = pagerState.currentPage < pages.lastIndex,
                    onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } }
                )
            }
            Spacer(Modifier.height(12.dp))
            PrimaryArrowButton(text = "Add meal", onClick = { onMeal(current.id) })
        }
    }
}

@Composable
private fun MealPage(meal: Meal, onClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        MealImage(
            name = meal.name,
            imageRes = meal.imageRes,
            modifier = Modifier.size(260.dp).clip(CircleShape).background(Color.White).clickable(onClick = onClick)
        )
        Spacer(Modifier.height(20.dp))
        Text(
            text = meal.category.label,
            color = MovaOrange,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clip(CircleShape).background(MovaOrangeSoft).padding(horizontal = 14.dp, vertical = 6.dp)
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = meal.name,
            color = MovaInk,
            fontSize = 27.sp,
            lineHeight = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(28.dp)) {
            Text("${meal.prepMinutes}min", color = MovaMuted, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Text("${meal.kcal}Kcal", color = MovaMuted, fontSize = 15.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun BoxScope.EdgeTab(glyph: String, atStart: Boolean, enabled: Boolean, onClick: () -> Unit) {
    val shape = if (atStart) {
        RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
    } else {
        RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp)
    }
    Box(
        modifier = Modifier
            .align(if (atStart) Alignment.CenterStart else Alignment.CenterEnd)
            .width(30.dp)
            .height(64.dp)
            .clip(shape)
            .background(MovaField)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(glyph, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = if (enabled) MovaInk else MovaMuted.copy(alpha = 0.4f))
    }
}

private fun sampleMeal(id: String, name: String, category: MealCategory, kcal: Int, min: Int) = Meal(
    id = id, name = name, category = category, suitableFor = setOf(MealType.LUNCH), dietTags = emptySet(),
    servingGrams = 100, macros = Macros(20.0, 30.0, 10.0), kcal = kcal, prepMinutes = min, imageRes = "meal_$id", tags = emptyList()
)

private val sampleCatalog = listOf(
    sampleMeal("1", "Avocado Dish", MealCategory.SALAD_VEGETABLES, 250, 30),
    sampleMeal("2", "Grilled Chicken Bowl", MealCategory.PROTEIN, 420, 25),
    sampleMeal("3", "Oatmeal with Berries", MealCategory.GRAINS, 310, 10)
)

@Preview(showBackground = true, heightDp = 780)
@Composable
private fun MealSuggestionPreview() {
    MealSuggestionContent(
        state = MealSuggestionUiState(catalog = sampleCatalog, suggestions = sampleCatalog.take(1), isLoading = false),
        onBack = {}, onMeal = {}, onSelectCategory = {}
    )
}

@Preview(showBackground = true, heightDp = 400)
@Composable
private fun MealSuggestionLoadingPreview() {
    MealSuggestionContent(MealSuggestionUiState(), {}, {}, {})
}
