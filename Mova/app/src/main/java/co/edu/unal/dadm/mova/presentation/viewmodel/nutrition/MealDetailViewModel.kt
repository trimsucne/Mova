package co.edu.unal.dadm.mova.presentation.viewmodel.nutrition

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.unal.dadm.mova.core.common.Result
import co.edu.unal.dadm.mova.domain.model.Meal
import co.edu.unal.dadm.mova.domain.model.MealLog
import co.edu.unal.dadm.mova.domain.model.MealType
import co.edu.unal.dadm.mova.domain.repository.MealRepository
import co.edu.unal.dadm.mova.domain.usecase.LogMealUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate

class MealDetailViewModel(
    mealId: String,
    date: LocalDate,
    today: LocalDate,
    private val mealRepository: MealRepository,
    private val logMeal: LogMealUseCase,
    private val currentUserId: () -> String,
    private val clock: () -> Instant = Instant::now
) : ViewModel() {

    /** No se registra en días futuros (spec §3). */
    private val logDate = if (date.isAfter(today)) today else date

    private val _state = MutableStateFlow(MealDetailUiState())
    val state: StateFlow<MealDetailUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            mealRepository.observeMeal(mealId).collect { meal ->
                _state.update { current ->
                    when {
                        meal == null -> current.copy(isLoading = false, error = "Meal not found")
                        current.meal == null -> current.withPortion(meal, meal.servingGrams).copy(
                            isLoading = false,
                            mealType = meal.suitableFor.minOrNull() ?: MealType.SNACK
                        )
                        else -> current.withPortion(meal, current.grams)
                    }
                }
            }
        }
    }

    fun onEvent(event: MealDetailEvent) {
        when (event) {
            is MealDetailEvent.GramsChanged -> _state.update { current ->
                current.meal?.let { current.withPortion(it, event.grams.coerceIn(MIN_GRAMS, MAX_GRAMS)) } ?: current
            }
            is MealDetailEvent.MealTypeChanged -> _state.update { it.copy(mealType = event.mealType) }
            MealDetailEvent.AddMeal -> addMeal()
            MealDetailEvent.ErrorShown -> _state.update { it.copy(error = null) }
        }
    }

    private fun addMeal() {
        val current = _state.value
        val meal = current.meal ?: return
        if (current.isSaving) return
        _state.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            val entry = MealLog.fromMeal(meal, current.grams, currentUserId(), logDate, current.mealType, clock())
            val result = logMeal(entry)
            _state.update {
                when (result) {
                    is Result.Success -> it.copy(isSaving = false, addedOn = logDate)
                    is Result.Error -> it.copy(isSaving = false, error = result.message)
                    Result.Loading -> it
                }
            }
        }
    }

    private fun MealDetailUiState.withPortion(meal: Meal, grams: Int): MealDetailUiState {
        val macros = meal.macros.scaled(grams.toDouble() / meal.servingGrams)
        return copy(meal = meal, grams = grams, macros = macros, kcal = macros.kcal)
    }

    private companion object {
        const val MIN_GRAMS = 1
        const val MAX_GRAMS = 2000
    }
}
