package co.edu.unal.dadm.mova.presentation.viewmodel.nutrition

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.unal.dadm.mova.core.common.Result
import co.edu.unal.dadm.mova.domain.model.Macros
import co.edu.unal.dadm.mova.domain.model.MealLog
import co.edu.unal.dadm.mova.domain.model.MealType
import co.edu.unal.dadm.mova.domain.usecase.CalculateKcalFromMacrosUseCase
import co.edu.unal.dadm.mova.domain.usecase.LogMealUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate

class AddMealViewModel(
    date: LocalDate,
    today: LocalDate,
    initialType: MealType?,
    private val logMeal: LogMealUseCase,
    private val calculateKcal: CalculateKcalFromMacrosUseCase,
    private val currentUserId: () -> String,
    private val clock: () -> Instant = Instant::now
) : ViewModel() {

    private val logDate = if (date.isAfter(today)) today else date

    private val _state = MutableStateFlow(AddMealUiState(mealType = initialType ?: MealType.BREAKFAST))
    val state: StateFlow<AddMealUiState> = _state.asStateFlow()

    fun onEvent(event: AddMealEvent) {
        when (event) {
            is AddMealEvent.NameChanged -> _state.update { it.copy(name = event.value, nameError = null) }
            is AddMealEvent.ProteinChanged -> updateMacros { it.copy(protein = event.value) }
            is AddMealEvent.CarbsChanged -> updateMacros { it.copy(carbs = event.value) }
            is AddMealEvent.FatChanged -> updateMacros { it.copy(fat = event.value) }
            is AddMealEvent.MealTypeChanged -> _state.update { it.copy(mealType = event.mealType) }
            AddMealEvent.Save -> save()
            AddMealEvent.ErrorShown -> _state.update { it.copy(error = null) }
        }
    }

    private fun updateMacros(change: (AddMealUiState) -> AddMealUiState) {
        _state.update { current ->
            val next = change(current)
            next.copy(kcal = calculateKcal(next.macrosOrZero()), macroError = null)
        }
    }

    private fun save() {
        val current = _state.value
        if (current.isSaving) return
        val name = current.name.trim()
        val nameError = if (name.length !in 2..60) "Name must be between 2 and 60 characters" else null
        val values = listOf(current.protein, current.carbs, current.fat).map(::parseGrams)
        val macroError = when {
            values.any { it == null || it !in 0.0..300.0 } -> "Each macro must be between 0 and 300 g"
            values.all { it == 0.0 } -> "Enter at least one macro greater than 0 g"
            else -> null
        }
        if (nameError != null || macroError != null) {
            _state.update { it.copy(nameError = nameError, macroError = macroError) }
            return
        }
        _state.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            val entry = MealLog.manual(name, current.macrosOrZero(), currentUserId(), logDate, current.mealType, clock())
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

    private fun AddMealUiState.macrosOrZero() =
        Macros(parseGrams(protein) ?: 0.0, parseGrams(carbs) ?: 0.0, parseGrams(fat) ?: 0.0)

    /** Vacío = 0; acepta coma decimal; null si no es un número. */
    private fun parseGrams(text: String): Double? =
        if (text.isBlank()) 0.0 else text.trim().replace(',', '.').toDoubleOrNull()?.takeIf { it.isFinite() }
}
