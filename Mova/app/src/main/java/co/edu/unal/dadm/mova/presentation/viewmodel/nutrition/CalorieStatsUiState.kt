package co.edu.unal.dadm.mova.presentation.viewmodel.nutrition

data class MacroShare(val proteinPct: Int, val carbsPct: Int, val fatPct: Int)

data class CalorieStatsUiState(
    val share: MacroShare? = null,
    val totalKcal: Int = 0,
    val isLoading: Boolean = true
)
