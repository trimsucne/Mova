package co.edu.unal.dadm.mova.presentation.screen.nutrition

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
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
import co.edu.unal.dadm.mova.presentation.theme.MovaError
import co.edu.unal.dadm.mova.presentation.theme.MovaField
import co.edu.unal.dadm.mova.presentation.theme.MovaInk
import co.edu.unal.dadm.mova.presentation.theme.MovaMuted
import co.edu.unal.dadm.mova.presentation.theme.MovaOrange
import co.edu.unal.dadm.mova.presentation.theme.MovaOrangeSoft
import co.edu.unal.dadm.mova.presentation.theme.MovaPurpleIcon
import co.edu.unal.dadm.mova.presentation.viewmodel.nutrition.MealDetailEvent
import co.edu.unal.dadm.mova.presentation.viewmodel.nutrition.MealDetailUiState
import co.edu.unal.dadm.mova.presentation.viewmodel.nutrition.MealDetailViewModel
import co.edu.unal.dadm.mova.presentation.viewmodel.nutrition.NutritionViewModelFactory
import java.time.LocalDate
import kotlin.math.max
import kotlin.math.roundToInt

private val ScreenBg = Color(0xFFF8F8F8)
private val ProteinGreen = Color(0xFF4CAF50)

@Composable
fun MealDetailScreen(
    container: NutritionContainer,
    mealId: String,
    date: LocalDate,
    onBack: () -> Unit,
    onAdded: (LocalDate) -> Unit
) {
    val viewModel: MealDetailViewModel = viewModel(factory = NutritionViewModelFactory {
        MealDetailViewModel(mealId, date, LocalDate.now(), container.mealRepository, container.logMeal, container.currentUserId)
    })
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.addedOn) { state.addedOn?.let(onAdded) }

    MealDetailContent(
        state = state,
        onBack = onBack,
        onGramsChanged = { viewModel.onEvent(MealDetailEvent.GramsChanged(it)) },
        onMealTypeChanged = { viewModel.onEvent(MealDetailEvent.MealTypeChanged(it)) },
        onAddMeal = { viewModel.onEvent(MealDetailEvent.AddMeal) }
    )
}

@Composable
private fun MealDetailContent(
    state: MealDetailUiState,
    onBack: () -> Unit,
    onGramsChanged: (Int) -> Unit,
    onMealTypeChanged: (MealType) -> Unit,
    onAddMeal: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBg)
            .verticalScroll(rememberScrollState())
    ) {
        DetailTopBar(onBack)
        val meal = state.meal
        when {
            state.isLoading -> Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            meal == null -> Text(
                state.error ?: "Meal not found",
                color = MovaMuted,
                modifier = Modifier.padding(20.dp)
            )
            else -> {
                MealImage(
                    meal.name, meal.imageRes,
                    Modifier.fillMaxWidth().height(260.dp).clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                )
                var gramsText by rememberSaveable(meal.id) { mutableStateOf(state.grams.toString()) }
                Column(
                    modifier = Modifier
                        .offset(y = (-28).dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                        .background(Color.White)
                        .padding(horizontal = 20.dp, vertical = 22.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            meal.name, color = MovaInk, fontSize = 22.sp, fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f), maxLines = 2
                        )
                        GramsField(
                            value = gramsText,
                            onValueChange = { text ->
                                gramsText = text.filter(Char::isDigit).take(4).let { digits ->
                                    if ((digits.toIntOrNull() ?: 0) > MealDetailViewModel.MAX_GRAMS) MealDetailViewModel.MAX_GRAMS.toString() else digits
                                }
                                gramsText.toIntOrNull()?.takeIf { it > 0 }?.let(onGramsChanged)
                            }
                        )
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Nutrition value", color = MovaMuted, fontSize = 14.sp)
                        Text("${state.kcal} cal", color = MovaMuted, fontSize = 14.sp)
                    }
                    val macros = state.macros
                    val maxMacro = max(1.0, max(macros.proteinG, max(macros.carbsG, macros.fatG)))
                    MacroRow("🌿", ProteinGreen, "Protein", macros.proteinG, maxMacro)
                    MacroRow("🥖", MovaOrange, "Carbs", macros.carbsG, maxMacro)
                    MacroRow("🫐", MovaPurpleIcon, "Fat", macros.fatG, maxMacro)
                    DetailMealTypeChips(selected = state.mealType, onSelect = onMealTypeChanged)
                    state.error?.let { Text(it, color = MovaError, fontSize = 14.sp) }
                    PrimaryArrowButton(
                        text = if (state.isSaving) "Adding..." else "Add Meal",
                        onClick = onAddMeal,
                        enabled = !state.isSaving && (gramsText.toIntOrNull() ?: 0) > 0
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircleButton("‹", onClick = onBack, fontSize = 24.sp)
        Text(
            "Nutrition", color = MovaInk, fontSize = 18.sp, fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center, modifier = Modifier.weight(1f)
        )
        CircleButton("•••", onClick = {}, fontSize = 14.sp)
    }
}

@Composable
private fun CircleButton(glyph: String, onClick: () -> Unit, fontSize: androidx.compose.ui.unit.TextUnit) {
    Box(
        modifier = Modifier.size(42.dp).clip(CircleShape).background(MovaField).clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(glyph, fontSize = fontSize, fontWeight = FontWeight.Bold, color = MovaInk)
    }
}

/** Gramos editables, con el mismo aspecto que el texto en negrita «100g». */
@Composable
private fun GramsField(value: String, onValueChange: (String) -> Unit) {
    val style = TextStyle(color = MovaInk, fontSize = 22.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
    Row(verticalAlignment = Alignment.CenterVertically) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = style,
            cursorBrush = SolidColor(MovaOrange),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.padding(start = 12.dp).width((value.length.coerceAtLeast(1) * 13 + 6).dp)
        )
        Text("g", style = style.copy(textAlign = TextAlign.Start))
    }
}

@Composable
private fun MacroRow(emoji: String, color: Color, label: String, grams: Double, maxGrams: Double) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
            modifier = Modifier.size(36.dp).clip(RoundedCornerShape(12.dp)).background(color.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) { Text(emoji, fontSize = 18.sp) }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(label, color = MovaInk, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Text("${grams.roundToInt()}g", color = MovaMuted, fontSize = 13.sp)
            }
            Box(Modifier.fillMaxWidth().height(4.dp).clip(CircleShape).background(MovaField)) {
                Box(
                    Modifier
                        .fillMaxWidth((grams / maxGrams).toFloat().coerceIn(0f, 1f))
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(color)
                )
            }
        }
    }
}

@Composable
private fun DetailMealTypeChips(selected: MealType, onSelect: (MealType) -> Unit) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MealType.entries.forEach { type ->
            val isSelected = type == selected
            Text(
                text = type.label,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isSelected) MovaOrange else MovaMuted,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (isSelected) MovaOrangeSoft else MovaField)
                    .clickable { onSelect(type) }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            )
        }
    }
}

private val sampleMeal = Meal(
    id = "1", name = "Avocado Dish", category = MealCategory.SALAD_VEGETABLES, suitableFor = setOf(MealType.LUNCH),
    dietTags = emptySet(), servingGrams = 100, macros = Macros(22.0, 40.0, 12.0), kcal = 457, prepMinutes = 30,
    imageRes = "meal_1", tags = emptyList()
)

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun MealDetailPreview() {
    MealDetailContent(
        state = MealDetailUiState(
            meal = sampleMeal, grams = 100, macros = Macros(22.0, 40.0, 12.0), kcal = 457,
            mealType = MealType.LUNCH, isLoading = false
        ),
        onBack = {}, onGramsChanged = {}, onMealTypeChanged = {}, onAddMeal = {}
    )
}

@Preview(showBackground = true, heightDp = 400)
@Composable
private fun MealDetailErrorPreview() {
    MealDetailContent(
        state = MealDetailUiState(isLoading = false, error = "Meal not found"),
        onBack = {}, onGramsChanged = {}, onMealTypeChanged = {}, onAddMeal = {}
    )
}
