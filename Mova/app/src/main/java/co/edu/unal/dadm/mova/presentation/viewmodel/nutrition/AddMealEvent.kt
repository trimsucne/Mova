package co.edu.unal.dadm.mova.presentation.viewmodel.nutrition

import co.edu.unal.dadm.mova.domain.model.MealType

sealed interface AddMealEvent {
    data class NameChanged(val value: String) : AddMealEvent
    data class ProteinChanged(val value: String) : AddMealEvent
    data class CarbsChanged(val value: String) : AddMealEvent
    data class FatChanged(val value: String) : AddMealEvent
    data class MealTypeChanged(val mealType: MealType) : AddMealEvent
    data object Save : AddMealEvent
    data object ErrorShown : AddMealEvent
}
