package co.edu.unal.dadm.mova.presentation.viewmodel.nutrition

import co.edu.unal.dadm.mova.domain.model.MealLog
import co.edu.unal.dadm.mova.domain.model.MealType
import java.time.LocalDate

data class MyMealsUiState(
    val selectedDate: LocalDate,
    val days: List<LocalDate>,
    val filter: MealType? = null,
    val logs: List<MealLog> = emptyList(),
    val pendingDelete: MealLog? = null,
    val error: String? = null
) {
    val visibleLogs: List<MealLog> get() = if (filter == null) logs else logs.filter { it.mealType == filter }
    val totalKcal: Int get() = visibleLogs.sumOf { it.kcal }
}
