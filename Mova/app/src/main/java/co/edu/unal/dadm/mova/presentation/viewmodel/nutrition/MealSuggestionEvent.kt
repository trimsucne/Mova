package co.edu.unal.dadm.mova.presentation.viewmodel.nutrition

import co.edu.unal.dadm.mova.domain.model.MealCategory

sealed interface MealSuggestionEvent {
    data class SelectCategory(val category: MealCategory?) : MealSuggestionEvent
}
