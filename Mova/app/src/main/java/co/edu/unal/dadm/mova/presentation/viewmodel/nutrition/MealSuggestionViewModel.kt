package co.edu.unal.dadm.mova.presentation.viewmodel.nutrition

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.unal.dadm.mova.domain.repository.MealRepository
import co.edu.unal.dadm.mova.domain.repository.ProfileRepository
import co.edu.unal.dadm.mova.domain.usecase.ObserveDailyIntakeUseCase
import co.edu.unal.dadm.mova.domain.usecase.SuggestMealsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

class MealSuggestionViewModel(
    today: LocalDate,
    mealRepository: MealRepository,
    profileRepository: ProfileRepository,
    observeDailyIntake: ObserveDailyIntakeUseCase,
    suggestMeals: SuggestMealsUseCase,
    currentUserId: () -> String
) : ViewModel() {

    private val _state = MutableStateFlow(MealSuggestionUiState())
    val state: StateFlow<MealSuggestionUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                mealRepository.observeCatalog(),
                profileRepository.observe(currentUserId()).onStart { emit(null) }.catch { emit(null) },
                observeDailyIntake(today)
            ) { catalog, profile, intake ->
                catalog to suggestMeals(catalog, profile?.diet, intake)
            }.catch {
                _state.update { it.copy(isLoading = false) }
            }.collect { (catalog, suggestions) ->
                _state.update { it.copy(catalog = catalog, suggestions = suggestions, isLoading = false) }
            }
        }
    }

    fun onEvent(event: MealSuggestionEvent) {
        when (event) {
            is MealSuggestionEvent.SelectCategory -> _state.update { it.copy(category = event.category) }
        }
    }
}
