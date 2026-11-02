package co.edu.unal.dadm.mova.presentation.viewmodel.nutrition

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.unal.dadm.mova.domain.repository.MealRepository
import co.edu.unal.dadm.mova.domain.usecase.ObserveDailyIntakeUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

class CalorieIntakeViewModel(
    today: LocalDate,
    observeDailyIntake: ObserveDailyIntakeUseCase,
    mealRepository: MealRepository,
    currentUserId: () -> String
) : ViewModel() {

    private val _state = MutableStateFlow(CalorieIntakeUiState())
    val state: StateFlow<CalorieIntakeUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            observeDailyIntake(today).catch { }.collect { intake -> _state.update { it.copy(intake = intake) } }
        }
        viewModelScope.launch {
            val monthStart = today.withDayOfMonth(1)
            mealRepository.observeLogs(currentUserId(), monthStart, today).catch { }.collect { logs ->
                val byDay = logs.groupBy { it.date }.mapValues { (_, dayLogs) -> dayLogs.sumOf { it.kcal } }
                val series = (1..today.dayOfMonth).map { day -> byDay[today.withDayOfMonth(day)] ?: 0 }
                _state.update { it.copy(monthlyKcal = series) }
            }
        }
    }
}
