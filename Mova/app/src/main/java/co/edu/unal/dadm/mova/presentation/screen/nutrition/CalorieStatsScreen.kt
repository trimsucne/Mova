package co.edu.unal.dadm.mova.presentation.screen.nutrition

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
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
import co.edu.unal.dadm.mova.presentation.theme.MovaField
import co.edu.unal.dadm.mova.presentation.theme.MovaInk
import co.edu.unal.dadm.mova.presentation.theme.MovaMuted
import co.edu.unal.dadm.mova.presentation.theme.MovaOrange
import co.edu.unal.dadm.mova.presentation.viewmodel.nutrition.CalorieStatsUiState
import co.edu.unal.dadm.mova.presentation.viewmodel.nutrition.CalorieStatsViewModel
import co.edu.unal.dadm.mova.presentation.viewmodel.nutrition.MacroShare
import co.edu.unal.dadm.mova.presentation.viewmodel.nutrition.NutritionViewModelFactory
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

private val ScreenBg = Color(0xFFF8F8F8)
private val TrackGrey = Color(0xFFD9D9D9)
private val StatBlue = Color(0xFF2563EB)

@Composable
fun CalorieStatsScreen(container: NutritionContainer, onBack: () -> Unit) {
    val viewModel: CalorieStatsViewModel = viewModel(factory = NutritionViewModelFactory {
        CalorieStatsViewModel(LocalDate.now(), container.mealRepository, container.currentUserId)
    })
    val state by viewModel.state.collectAsStateWithLifecycle()
    CalorieStatsContent(state = state, today = LocalDate.now(), onBack = onBack)
}

@Composable
private fun CalorieStatsContent(state: CalorieStatsUiState, today: LocalDate, onBack: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(ScreenBg)) {
        Column(
            modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(44.dp)
                        .clip(CircleShape)
                        .border(BorderStroke(1.dp, Color(0xFFD0D0D4)), CircleShape)
                        .clickable(onClick = onBack),
                    contentAlignment = Alignment.Center
                ) { Text("←", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MovaInk) }
                Text("Calorie Stats", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = MovaInk)
            }
            val share = state.share
            when {
                state.isLoading -> Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                share == null -> Text("No meals logged this month yet.", color = MovaMuted)
                else -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("%,d".format(state.totalKcal), fontSize = 56.sp, fontWeight = FontWeight.ExtraBold, color = MovaInk)
                            Text("kcal", fontSize = 32.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF9A9AA0), modifier = Modifier.padding(bottom = 6.dp))
                        }
                        Text(
                            text = today.month.getDisplayName(TextStyle.SHORT, Locale.ENGLISH),
                            fontWeight = FontWeight.SemiBold,
                            color = MovaInk,
                            modifier = Modifier.clip(RoundedCornerShape(50)).background(MovaField).padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatBar("Fat", share.fatPct, MovaInk, Modifier.weight(1f))
                        StatBar("Protein", share.proteinPct, StatBlue, Modifier.weight(1f))
                        StatBar("Carbs", share.carbsPct, MovaOrange, Modifier.weight(1f))
                    }
                    Column {
                        LegendRow("Fat", MovaInk, state.monthMacros.fatG)
                        HorizontalDivider(color = Color(0xFFE6E6E8))
                        LegendRow("Protein", StatBlue, state.monthMacros.proteinG)
                        HorizontalDivider(color = Color(0xFFE6E6E8))
                        LegendRow("Carbs", MovaOrange, state.monthMacros.carbsG)
                    }
                }
            }
        }
        // Insignia visual (D6): N9 no se bloquea en el prototipo.
        Text(
            text = "PREMIUM",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(20.dp)
                .background(MovaOrange, RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun StatBar(label: String, percent: Int, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.height(260.dp).clip(RoundedCornerShape(28.dp)).background(TrackGrey),
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight((percent / 100f).coerceIn(0.12f, 1f))
                .clip(RoundedCornerShape(28.dp))
                .background(color),
            contentAlignment = Alignment.BottomCenter
        ) {
            Text(
                "$percent%",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
    }
}

@Composable
private fun LegendRow(name: String, color: Color, grams: Double) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(16.dp).clip(CircleShape).background(color))
        Text(name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MovaInk, modifier = Modifier.weight(1f).padding(start = 12.dp))
        Text("${grams.toInt()}g", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MovaMuted)
    }
}

@Preview(showBackground = true, heightDp = 780)
@Composable
private fun CalorieStatsPreview() {
    CalorieStatsContent(
        state = CalorieStatsUiState(
            share = MacroShare(proteinPct = 30, carbsPct = 50, fatPct = 20),
            totalKcal = 24350,
            monthMacros = Macros(proteinG = 201.0, carbsG = 340.0, fatG = 95.0),
            isLoading = false
        ),
        today = LocalDate.of(2026, 1, 20),
        onBack = {}
    )
}

@Preview(showBackground = true, heightDp = 400)
@Composable
private fun CalorieStatsEmptyPreview() {
    CalorieStatsContent(state = CalorieStatsUiState(isLoading = false), today = LocalDate.of(2026, 1, 20), onBack = {})
}
