package co.edu.unal.dadm.mova.presentation.viewmodel.nutrition

import co.edu.unal.dadm.mova.domain.model.DailyIntake

data class CalorieIntakeUiState(
    val intake: DailyIntake? = null,
    /** kcal consumidas por día del mes actual, desde el día 1 hasta hoy. */
    val monthlyKcal: List<Int> = emptyList()
) {
    val exceededBy: Int? get() = intake?.remainingKcal?.let { if (it < 0) -it else null }
}
