package co.edu.unal.dadm.mova.presentation.screen.progress

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.unal.dadm.mova.presentation.charts.BarChartWeekly
import co.edu.unal.dadm.mova.presentation.charts.DonutChart
import co.edu.unal.dadm.mova.presentation.components.MovaBottomBar
import co.edu.unal.dadm.mova.presentation.theme.MovaColor
import co.edu.unal.dadm.mova.presentation.viewmodel.progress.UpliftScoreViewModel

@Composable
fun UpliftScoreScreenRoute(
    userId: String,
    onNavigateTab: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: UpliftScoreViewModel = viewModel(
        factory = UpliftScoreViewModel.Factory(userId)
    )
    val uiState by viewModel.uiState.collectAsState()

    UpliftScoreScreen(
        uiState = uiState,
        onNavigateTab = onNavigateTab,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpliftScoreScreen(
    uiState: co.edu.unal.dadm.mova.presentation.viewmodel.progress.UpliftScoreUiState,
    onNavigateTab: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("Uplift Score 🌟", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MovaColor.TextBlack)
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MovaColor.BackgroundWhite)
            )
        },
        bottomBar = {
            MovaBottomBar(
                currentRoute = "uplift_score",
                onNavigate = onNavigateTab
            )
        },
        containerColor = MovaColor.BackgroundWhite,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = MovaColor.CardLightGray)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DonutChart(score = uiState.score)

                    Surface(
                        color = MovaColor.ScoreGreenLight,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Health Status: ${uiState.healthStatus}",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MovaColor.ScoreGreen
                        )
                    }
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = MovaColor.CardLightGray)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Performance Indicators", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MovaColor.TextBlack)

                    ScoreIndicatorRow(label = "Strength 💪", score = uiState.strength, color = MovaColor.PrimaryOrange)
                    ScoreIndicatorRow(label = "Agility ⚡", score = uiState.agility, color = MovaColor.HydrationBlue)
                    ScoreIndicatorRow(label = "Endurance 🫁", score = uiState.endurance, color = MovaColor.ScoreGreen)
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = MovaColor.CardLightGray)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Weekly Score Trend", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MovaColor.TextBlack)
                        Text("Mon - Sun", fontSize = 12.sp, color = MovaColor.TextSecondary)
                    }

                    BarChartWeekly(weeklyData = uiState.weeklyProgress)
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = MovaColor.PrimaryOrangeLight)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Weekly Nutritional Adherence 🥗",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MovaColor.PrimaryOrangeDark
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${uiState.weeklyNutritionalAdherencePercent}% Adherence",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = MovaColor.TextBlack
                            )
                            Text(
                                text = "${uiState.loggedMealsCount} meals logged this week",
                                fontSize = 12.sp,
                                color = MovaColor.TextDarkGray
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ScoreIndicatorRow(
    label: String,
    score: Int,
    color: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MovaColor.TextBlack)
            Text("$score%", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = color)
        }
        LinearProgressIndicator(
            progress = { (score.toFloat() / 100f).coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = MovaColor.CardBorder
        )
    }
}

