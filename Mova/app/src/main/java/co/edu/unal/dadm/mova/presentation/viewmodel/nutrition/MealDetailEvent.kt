package co.edu.unal.dadm.mova.presentation.viewmodel.nutrition

import co.edu.unal.dadm.mova.domain.model.MealType

sealed interface MealDetailEvent {
    data class GramsChanged(val grams: Int) : MealDetailEvent
    data class MealTypeChanged(val mealType: MealType) : MealDetailEvent
    data object AddMeal : MealDetailEvent
    data object ErrorShown : MealDetailEvent
}
