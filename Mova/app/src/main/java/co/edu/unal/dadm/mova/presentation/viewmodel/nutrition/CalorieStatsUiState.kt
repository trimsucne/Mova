package co.edu.unal.dadm.mova.presentation.viewmodel.nutrition

import co.edu.unal.dadm.mova.domain.model.Macros

data class MacroShare(val proteinPct: Int, val carbsPct: Int, val fatPct: Int)

data class CalorieStatsUiState(
    val share: MacroShare? = null,
    val totalKcal: Int = 0,
    /** Gramos totales de cada macro en el mes actual. */
    val monthMacros: Macros = Macros.ZERO,
    val isLoading: Boolean = true
)
