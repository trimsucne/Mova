package co.edu.unal.dadm.mova.presentation.viewmodel.nutrition

import co.edu.unal.dadm.mova.domain.model.MealType
import java.time.LocalDate

data class AddMealUiState(
    val name: String = "",
    val protein: String = "",
    val carbs: String = "",
    val fat: String = "",
    val mealType: MealType = MealType.BREAKFAST,
    val kcal: Int = 0,
    val nameError: String? = null,
    val macroError: String? = null,
    val isSaving: Boolean = false,
    val addedOn: LocalDate? = null,
    val error: String? = null
)
