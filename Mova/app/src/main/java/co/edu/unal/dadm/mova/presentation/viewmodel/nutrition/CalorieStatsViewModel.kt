package co.edu.unal.dadm.mova.presentation.viewmodel.nutrition

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.unal.dadm.mova.domain.model.Macros
import co.edu.unal.dadm.mova.domain.repository.MealRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import kotlin.math.roundToInt

class CalorieStatsViewModel(
    today: LocalDate,
    mealRepository: MealRepository,
    currentUserId: () -> String
) : ViewModel() {

    private val _state = MutableStateFlow(CalorieStatsUiState())
    val state: StateFlow<CalorieStatsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            mealRepository.observeLogs(currentUserId(), today.withDayOfMonth(1), today).collect { logs ->
                val total = logs.fold(Macros.ZERO) { sum, log -> sum + log.macros }
                _state.update { it.copy(share = share(total), totalKcal = logs.sumOf { log -> log.kcal }, isLoading = false) }
            }
        }
    }

    companion object {
        /** Porcentaje de kcal de cada macro; la grasa absorbe el redondeo para que sumen 100. */
        fun share(macros: Macros): MacroShare? {
            val protein = macros.proteinG * 4
            val carbs = macros.carbsG * 4
            val fat = macros.fatG * 9
            val total = protein + carbs + fat
            if (total <= 0.0) return null
            val proteinPct = (protein / total * 100).roundToInt()
            val carbsPct = (carbs / total * 100).roundToInt()
            return MacroShare(proteinPct, carbsPct, 100 - proteinPct - carbsPct)
        }
    }
}
