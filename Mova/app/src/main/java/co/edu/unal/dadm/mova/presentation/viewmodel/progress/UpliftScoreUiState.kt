package co.edu.unal.dadm.mova.presentation.viewmodel.progress

import co.edu.unal.dadm.mova.presentation.charts.WeeklyBarData

data class UpliftScoreUiState(
    val score: Int = 84,
    val healthStatus: String = "Optimal Health",
    val strength: Int = 88,
    val agility: Int = 76,
    val endurance: Int = 82,
    val weeklyProgress: List<WeeklyBarData> = listOf(
        WeeklyBarData("Mon", 78),
        WeeklyBarData("Tue", 80),
        WeeklyBarData("Wed", 79),
        WeeklyBarData("Thu", 82),
        WeeklyBarData("Fri", 81),
        WeeklyBarData("Sat", 85),
        WeeklyBarData("Sun", 84, isHighlighted = true)
    ),
    val weeklyNutritionalAdherencePercent: Int = 88,
    val loggedMealsCount: Int = 14,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

