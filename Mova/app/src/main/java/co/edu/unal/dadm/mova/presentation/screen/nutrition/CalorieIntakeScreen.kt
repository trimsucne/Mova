package co.edu.unal.dadm.mova.presentation.screen.nutrition

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.unal.dadm.mova.core.di.NutritionContainer
import co.edu.unal.dadm.mova.domain.model.DailyIntake
import co.edu.unal.dadm.mova.domain.model.Macros
import co.edu.unal.dadm.mova.presentation.charts.SmoothAreaChart
import co.edu.unal.dadm.mova.presentation.screen.nutrition.components.RoundedSquareBackButton
import co.edu.unal.dadm.mova.presentation.theme.MovaField
import co.edu.unal.dadm.mova.presentation.theme.MovaInk
import co.edu.unal.dadm.mova.presentation.theme.MovaMuted
import co.edu.unal.dadm.mova.presentation.viewmodel.nutrition.CalorieIntakeUiState
import co.edu.unal.dadm.mova.presentation.viewmodel.nutrition.CalorieIntakeViewModel
import co.edu.unal.dadm.mova.presentation.viewmodel.nutrition.NutritionViewModelFactory
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

private val ScreenBg = Color(0xFFF8F8F8)
private val SoftOrange = Color(0xFFFFF0E6)
private val SoftBlue = Color(0xFFE5EDFF)
private val SoftGreen = Color(0xFFE9F8E2)

@Composable
fun CalorieIntakeScreen(
    container: NutritionContainer,
    onBack: () -> Unit,
    onMyMeals: () -> Unit,
    onAddMeal: () -> Unit,
    onExplore: () -> Unit,
    onStats: () -> Unit
) {
    val viewModel: CalorieIntakeViewModel = viewModel(factory = NutritionViewModelFactory {
        CalorieIntakeViewModel(LocalDate.now(), container.observeDailyIntake, container.mealRepository, container.currentUserId)
    })
    val state by viewModel.state.collectAsStateWithLifecycle()
    CalorieIntakeContent(
        state = state,
        today = LocalDate.now(),
        onBack = onBack,
        onMyMeals = onMyMeals,
        onAddMeal = onAddMeal,
        onExplore = onExplore,
        onStats = onStats
    )
}

@Composable
private fun CalorieIntakeContent(
    state: CalorieIntakeUiState,
    today: LocalDate,
    onBack: () -> Unit,
    onMyMeals: () -> Unit,
    onAddMeal: () -> Unit,
    onExplore: () -> Unit,
    onStats: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RoundedSquareBackButton(onClick = onBack)
            Text(
                "Calorie Intake",
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MovaInk
            )
            Box(
                modifier = Modifier.size(46.dp).clip(RoundedCornerShape(14.dp)).background(MovaField).clickable(onClick = onStats),
                contentAlignment = Alignment.Center
            ) { Text("⚙", fontSize = 22.sp, color = MovaInk) }
        }

        val intake = state.intake
        if (intake == null) {
            Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("🔥", fontSize = 40.sp)
                    Text("%,d".format(intake.consumedKcal), fontSize = 56.sp, fontWeight = FontWeight.ExtraBold, color = MovaInk)
                    Text("Kcal", fontSize = 32.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF9A9AA0))
                }
                val exceeded = state.exceededBy
                Text(
                    text = if (exceeded != null) "Hoy superaste tu meta por $exceeded kcal; mañana se equilibra"
                    else "Eat ${intake.remainingKcal} calorie left.",
                    color = MovaMuted,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )
                if (intake.burnedKcal > 0) {
                    Text("Goal includes ${intake.burnedKcal} kcal burned today", color = MovaMuted, fontSize = 13.sp)
                }
            }
        }

        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Row(
                modifier = Modifier.clip(RoundedCornerShape(50)).background(MovaField).padding(horizontal = 18.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("📅", fontSize = 16.sp)
                Text(
                    "${today.month.getDisplayName(TextStyle.FULL, Locale.ENGLISH)} ${today.year}",
                    fontWeight = FontWeight.SemiBold,
                    color = MovaInk
                )
                Text("⌄", fontWeight = FontWeight.Bold, color = MovaMuted)
            }
        }

        Column(Modifier.fillMaxWidth()) {
            val monthly = state.monthlyKcal
            if (monthly.isNotEmpty()) {
                SmoothAreaChart(
                    values = monthly.map { it.toFloat() },
                    modifier = Modifier.fillMaxWidth().height(300.dp).padding(start = 8.dp, end = 16.dp, bottom = 8.dp),
                    gradientFill = true,
                    gridLevels = 4,
                    highlightIndex = monthly.lastIndex,
                    highlightLabel = "%,dKcal".format(monthly.last())
                )
            } else {
                Spacer(Modifier.fillMaxWidth().height(300.dp))
            }
            if (intake != null) {
                MacroCard(
                    consumed = intake.consumed,
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .layout { measurable, constraints ->
                            val placeable = measurable.measure(constraints)
                            val overlap = 24.dp.roundToPx().coerceAtMost(placeable.height)
                            layout(placeable.width, placeable.height - overlap) { placeable.place(0, -overlap) }
                        }
                )
            }
        }
        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            NavPill("My Meals", onMyMeals, Modifier.weight(1f))
            NavPill("Add Meal", onAddMeal, Modifier.weight(1f))
            NavPill("Explore", onExplore, Modifier.weight(1f))
        }
    }
}

@Composable
private fun MacroCard(consumed: Macros, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .shadow(10.dp, RoundedCornerShape(24.dp), ambientColor = Color(0x22000000), spotColor = Color(0x22000000))
            .background(Color.White, RoundedCornerShape(24.dp))
            .padding(vertical = 18.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        MacroColumn("🥖", SoftOrange, "${consumed.carbsG.toInt()}g", "Carbs")
        MacroColumn("🍎", SoftBlue, "${consumed.proteinG.toInt()}g", "Protein")
        MacroColumn("💪", SoftGreen, "${consumed.fatG.toInt()}g", "Fats")
    }
}

@Composable
private fun MacroColumn(icon: String, iconBg: Color, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(iconBg), contentAlignment = Alignment.Center) {
            Text(icon, fontSize = 20.sp)
        }
        Text(value, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = MovaInk)
        Text(label, color = MovaMuted, fontSize = 14.sp)
    }
}

@Composable
private fun NavPill(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.height(40.dp).clip(RoundedCornerShape(50)).background(MovaField).clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MovaInk)
    }
}

private fun sampleIntake(remaining: Int) = DailyIntake(
    date = LocalDate.of(2026, 8, 19),
    consumed = Macros(proteinG = 98.0, carbsG = 125.0, fatG = 62.0),
    consumedKcal = 1745,
    goalKcal = 1745 + remaining,
    burnedKcal = 240,
    remainingKcal = remaining,
    goalMacros = Macros(120.0, 200.0, 70.0),
    byMealType = emptyMap()
)

private val sampleMonth = listOf(1200, 1500, 900, 1800, 1650, 1300, 1950, 1400, 1700, 1250, 1600, 1500, 1800, 1578, 1745)

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun CalorieIntakePreview() {
    CalorieIntakeContent(
        state = CalorieIntakeUiState(sampleIntake(255), sampleMonth),
        today = LocalDate.of(2026, 8, 19),
        onBack = {}, onMyMeals = {}, onAddMeal = {}, onExplore = {}, onStats = {}
    )
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun CalorieIntakeExceededPreview() {
    CalorieIntakeContent(
        state = CalorieIntakeUiState(sampleIntake(-120), sampleMonth),
        today = LocalDate.of(2026, 8, 19),
        onBack = {}, onMyMeals = {}, onAddMeal = {}, onExplore = {}, onStats = {}
    )
}
