package co.edu.unal.dadm.mova.presentation.screen.nutrition

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.unal.dadm.mova.core.di.NutritionContainer
import co.edu.unal.dadm.mova.domain.model.Macros
import co.edu.unal.dadm.mova.domain.model.MealLog
import co.edu.unal.dadm.mova.domain.model.MealSource
import co.edu.unal.dadm.mova.domain.model.MealType
import co.edu.unal.dadm.mova.presentation.screen.nutrition.components.PrimaryArrowButton
import co.edu.unal.dadm.mova.presentation.screen.nutrition.components.RoundedSquareBackButton
import co.edu.unal.dadm.mova.presentation.theme.MovaField
import co.edu.unal.dadm.mova.presentation.theme.MovaInk
import co.edu.unal.dadm.mova.presentation.theme.MovaMuted
import co.edu.unal.dadm.mova.presentation.theme.MovaOrange
import co.edu.unal.dadm.mova.presentation.theme.MovaTheme
import co.edu.unal.dadm.mova.presentation.viewmodel.nutrition.MyMealsEvent
import co.edu.unal.dadm.mova.presentation.viewmodel.nutrition.MyMealsUiState
import co.edu.unal.dadm.mova.presentation.viewmodel.nutrition.MyMealsViewModel
import co.edu.unal.dadm.mova.presentation.viewmodel.nutrition.NutritionViewModelFactory
import java.time.Instant
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.roundToInt

private val ScreenBg = Color(0xFFF8F8F8)
private val CardBorder = Color(0xFFEEEEEE)
private val ProteinGreen = Color(0xFF4CAF50)
private val FatsRed = Color(0xFFEF6B3C)
private val CarbsYellow = Color(0xFFF5C518)

@Composable
fun MyMealsScreen(
    container: NutritionContainer,
    initialDate: LocalDate,
    onBack: () -> Unit,
    onAddMeal: (MealType?, LocalDate) -> Unit,
    onExplore: () -> Unit
) {
    val viewModel: MyMealsViewModel = viewModel(factory = NutritionViewModelFactory {
        MyMealsViewModel(initialDate, LocalDate.now(), container.mealRepository, container.currentUserId)
    })
    val state by viewModel.state.collectAsStateWithLifecycle()
    MyMealsContent(
        state = state,
        onBack = onBack,
        onSelectDate = { viewModel.onEvent(MyMealsEvent.SelectDate(it)) },
        onSelectFilter = { viewModel.onEvent(MyMealsEvent.SelectFilter(it)) },
        onRequestDelete = { viewModel.onEvent(MyMealsEvent.RequestDelete(it)) },
        onConfirmDelete = { viewModel.onEvent(MyMealsEvent.ConfirmDelete) },
        onCancelDelete = { viewModel.onEvent(MyMealsEvent.CancelDelete) },
        onAddMeal = { onAddMeal(state.filter, state.selectedDate) },
        onExplore = onExplore
    )
}

@Composable
private fun MyMealsContent(
    state: MyMealsUiState,
    onBack: () -> Unit,
    onSelectDate: (LocalDate) -> Unit,
    onSelectFilter: (MealType?) -> Unit,
    onRequestDelete: (MealLog) -> Unit,
    onConfirmDelete: () -> Unit,
    onCancelDelete: () -> Unit,
    onAddMeal: () -> Unit,
    onExplore: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().background(ScreenBg).padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            RoundedSquareBackButton(onClick = onBack)
            Text("My Meals", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MovaInk)
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(state.days) { day ->
                DateTile(day = day, selected = day == state.selectedDate, onClick = { onSelectDate(day) })
            }
        }
        FilterSegments(selected = state.filter, onSelect = onSelectFilter)
        Text("${state.totalKcal} kcal", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MovaMuted)
        state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        if (state.visibleLogs.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    "No meals logged for this day yet.",
                    color = MovaMuted,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(state.visibleLogs, key = { it.id }) { log ->
                    MealLogCard(log = log, onMore = { onRequestDelete(log) })
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            PrimaryArrowButton(text = "Add Meal", onClick = onAddMeal, modifier = Modifier.fillMaxWidth())
            OutlinedButton(
                onClick = onExplore,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.5.dp, MovaOrange)
            ) {
                Text("Explore", color = MovaOrange, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            }
        }
    }

    state.pendingDelete?.let { log ->
        AlertDialog(
            onDismissRequest = onCancelDelete,
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White,
            title = { Text("Delete meal?", fontWeight = FontWeight.Bold) },
            text = { Text("${log.name} will be removed from ${log.date}.", color = MovaMuted) },
            confirmButton = { TextButton(onClick = onConfirmDelete) { Text("Delete", color = MovaOrange, fontWeight = FontWeight.Bold) } },
            dismissButton = { TextButton(onClick = onCancelDelete) { Text("Cancel", color = MovaMuted) } }
        )
    }
}

@Composable
private fun DateTile(day: LocalDate, selected: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .size(width = 64.dp, height = 72.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) MovaOrange else MovaField)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            day.month.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
            fontSize = 13.sp,
            color = if (selected) Color.White else MovaMuted
        )
        Text(
            day.dayOfMonth.toString(),
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = if (selected) Color.White else MovaInk
        )
    }
}

@Composable
private fun FilterSegments(selected: MealType?, onSelect: (MealType?) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(50))
            .background(MovaField)
            .horizontalScroll(rememberScrollState())
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        val options: List<Pair<String, MealType?>> =
            listOf<Pair<String, MealType?>>("All" to null) + MealType.entries.map { it.label to it }
        options.forEach { (label, type) ->
            val isSelected = selected == type
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(if (isSelected) Color.White else Color.Transparent)
                    .clickable { onSelect(type) }
                    .padding(horizontal = 16.dp, vertical = 9.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    label,
                    fontSize = 14.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) MovaInk else MovaMuted
                )
            }
        }
    }
}

@Composable
private fun MealLogCard(log: MealLog, onMore: () -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Color.White)
            .border(1.dp, CardBorder, shape)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(36.dp).clip(CircleShape).background(MovaField),
                contentAlignment = Alignment.Center
            ) { Text("🍽", fontSize = 18.sp) }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(log.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MovaInk, maxLines = 1)
                val grams = log.grams?.let { " - ${it}g" }.orEmpty()
                Text("🔥 ${log.kcal} kcal$grams", fontSize = 13.sp, color = MovaMuted)
            }
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .border(1.dp, CardBorder, CircleShape)
                    .clickable(onClick = onMore),
                contentAlignment = Alignment.Center
            ) { Text("•••", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MovaInk) }
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            MacroColumn("${log.macros.proteinG.roundToInt()}g", "Protein", ProteinGreen, Modifier.weight(1f))
            MacroColumn("${log.macros.fatG.roundToInt()}g", "Fats", FatsRed, Modifier.weight(1f))
            MacroColumn("${log.macros.carbsG.roundToInt()}g", "Carbs", CarbsYellow, Modifier.weight(1f))
        }
    }
}

@Composable
private fun MacroColumn(value: String, label: String, color: Color, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.width(3.dp).height(32.dp).clip(RoundedCornerShape(2.dp)).background(color))
        Spacer(Modifier.width(8.dp))
        Column {
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MovaInk)
            Text(label, fontSize = 12.sp, color = MovaMuted)
        }
    }
}

private val sampleDay = LocalDate.of(2026, 8, 12)

private fun sampleLog(id: String, name: String, type: MealType, grams: Int?, p: Double, c: Double, f: Double) = MealLog(
    id = id, userId = "u", date = sampleDay, mealType = type, source = MealSource.CATALOG, mealId = id,
    name = name, grams = grams, macros = Macros(p, c, f), kcal = Macros(p, c, f).kcal,
    createdAt = Instant.EPOCH, updatedAt = Instant.EPOCH
)

private val sampleState = MyMealsUiState(
    selectedDate = sampleDay,
    days = (0..5L).map { sampleDay.minusDays(3).plusDays(it) },
    logs = listOf(
        sampleLog("1", "Avocado Dish", MealType.BREAKFAST, 100, 12.0, 20.0, 15.0),
        sampleLog("2", "Grilled Chicken Salad", MealType.LUNCH, null, 35.0, 12.0, 8.0)
    )
)

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun MyMealsPreview() {
    MovaTheme {
        MyMealsContent(sampleState, {}, {}, {}, {}, {}, {}, {}, {})
    }
}

@Preview(showBackground = true, heightDp = 600)
@Composable
private fun MyMealsEmptyPreview() {
    MovaTheme {
        MyMealsContent(sampleState.copy(logs = emptyList()), {}, {}, {}, {}, {}, {}, {}, {})
    }
}
