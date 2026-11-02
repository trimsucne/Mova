package co.edu.unal.dadm.mova.presentation.viewmodel.nutrition

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.unal.dadm.mova.core.common.Result
import co.edu.unal.dadm.mova.domain.repository.MealRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class MyMealsViewModel(
    initialDate: LocalDate,
    today: LocalDate,
    private val mealRepository: MealRepository,
    private val currentUserId: () -> String
) : ViewModel() {

    private val _state = MutableStateFlow(
        MyMealsUiState(selectedDate = minOf(initialDate, today), days = window(initialDate, today))
    )
    val state: StateFlow<MyMealsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            _state.map { it.selectedDate }
                .distinctUntilChanged()
                .flatMapLatest { date -> mealRepository.observeLogs(currentUserId(), date) }
                .collect { logs -> _state.update { it.copy(logs = logs) } }
        }
    }

    fun onEvent(event: MyMealsEvent) {
        when (event) {
            is MyMealsEvent.SelectDate -> _state.update { it.copy(selectedDate = event.date) }
            is MyMealsEvent.SelectFilter -> _state.update { it.copy(filter = event.mealType) }
            is MyMealsEvent.RequestDelete -> _state.update { it.copy(pendingDelete = event.log) }
            MyMealsEvent.CancelDelete -> _state.update { it.copy(pendingDelete = null) }
            MyMealsEvent.ConfirmDelete -> confirmDelete()
            MyMealsEvent.ErrorShown -> _state.update { it.copy(error = null) }
        }
    }

    private fun confirmDelete() {
        val target = _state.value.pendingDelete ?: return
        _state.update { it.copy(pendingDelete = null) }
        viewModelScope.launch {
            val result = mealRepository.delete(target.id)
            if (result is Result.Error) _state.update { it.copy(error = result.message) }
        }
    }

    private companion object {
        fun window(initialDate: LocalDate, today: LocalDate): List<LocalDate> {
            val end = minOf(initialDate.plusDays(6), today)
            return (6 downTo 0).map { end.minusDays(it.toLong()) }
        }
    }
}
