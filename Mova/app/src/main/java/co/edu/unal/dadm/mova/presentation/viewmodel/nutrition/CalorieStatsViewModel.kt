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
import kotlin.math.floor

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
        /**
         * Porcentaje de kcal de cada macro con reparto por el método del resto mayor:
         * se toma la parte entera de cada porcentaje y los puntos sobrantes van, uno a uno,
         * a los macros con mayor fracción. Así siempre son enteros no negativos que suman 100.
         */
        fun share(macros: Macros): MacroShare? {
            val kcal = doubleArrayOf(macros.proteinG * 4, macros.carbsG * 4, macros.fatG * 9)
            val total = kcal.sum()
            if (total <= 0.0) return null
            val exact = kcal.map { it / total * 100 }
            val pcts = exact.map { floor(it).toInt() }.toIntArray()
            var leftover = 100 - pcts.sum()
            // sortedByDescending es estable: en empates gana el macro que aparece primero.
            val order = exact.indices.sortedByDescending { exact[it] - pcts[it] }
            for (i in order) {
                if (leftover == 0) break
                pcts[i]++
                leftover--
            }
            return MacroShare(pcts[0], pcts[1], pcts[2])
        }
    }
}
