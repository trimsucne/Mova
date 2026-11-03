package co.edu.unal.dadm.mova.presentation.viewmodel.nutrition

import co.edu.unal.dadm.mova.domain.model.Macros
import co.edu.unal.dadm.mova.domain.model.Meal
import co.edu.unal.dadm.mova.domain.model.MealType
import java.time.LocalDate

data class MealDetailUiState(
    val meal: Meal? = null,
    val grams: Int = 0,
    val macros: Macros = Macros.ZERO,
    val kcal: Int = 0,
    val mealType: MealType = MealType.BREAKFAST,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    /** Fecha en que se registró; la pantalla navega a «Food Added!» cuando deja de ser null. */
    val addedOn: LocalDate? = null,
    val error: String? = null
)
