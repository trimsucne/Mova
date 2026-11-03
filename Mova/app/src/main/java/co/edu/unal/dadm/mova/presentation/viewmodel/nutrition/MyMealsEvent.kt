package co.edu.unal.dadm.mova.presentation.viewmodel.nutrition

import co.edu.unal.dadm.mova.domain.model.MealLog
import co.edu.unal.dadm.mova.domain.model.MealType
import java.time.LocalDate

sealed interface MyMealsEvent {
    data class SelectDate(val date: LocalDate) : MyMealsEvent
    data class SelectFilter(val mealType: MealType?) : MyMealsEvent
    data class RequestDelete(val log: MealLog) : MyMealsEvent
    data object ConfirmDelete : MyMealsEvent
    data object CancelDelete : MyMealsEvent
    data object ErrorShown : MyMealsEvent
}
