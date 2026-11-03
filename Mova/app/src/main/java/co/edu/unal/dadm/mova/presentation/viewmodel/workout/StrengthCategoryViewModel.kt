package co.edu.unal.dadm.mova.presentation.viewmodel.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.unal.dadm.mova.domain.model.WorkoutCategory
import co.edu.unal.dadm.mova.domain.usecase.ObserveWorkoutsUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class StrengthCategoryViewModel(
    category: WorkoutCategory,
    observeWorkouts: ObserveWorkoutsUseCase
) : ViewModel() {
    val state: StateFlow<StrengthCategoryUiState> = observeWorkouts(category)
        .map { StrengthCategoryUiState(loading = false, category = category, workouts = it) }
        .catch { emit(StrengthCategoryUiState(loading = false, category = category, error = it.message ?: "Error")) }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            StrengthCategoryUiState(category = category)
        )

    /** La navegacion la resuelve la Route; no hay eventos que muten estado. */
    fun onEvent(event: StrengthCategoryEvent) = Unit
}
