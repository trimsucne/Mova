package co.edu.unal.dadm.mova.presentation.viewmodel.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.unal.dadm.mova.domain.model.Diet
import co.edu.unal.dadm.mova.domain.model.FitnessLevel
import co.edu.unal.dadm.mova.domain.model.Gender
import co.edu.unal.dadm.mova.domain.model.Goal
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import co.edu.unal.dadm.mova.domain.usecase.SaveAssessmentUseCase

class OnboardingViewModel(
    private val saveAssessmentUseCase: SaveAssessmentUseCase? = null,
    private val userId: String? = null
) : ViewModel() {
    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun onEvent(event: OnboardingEvent) {
        val draft = _uiState.value.draft
        val updated = when (event) {
            is OnboardingEvent.SetAge -> draft.copy(age = event.age)
            is OnboardingEvent.SetWeight -> draft.copy(weightKg = event.weightKg)
            is OnboardingEvent.SetDiet -> draft.copy(diet = event.diet)
            is OnboardingEvent.SetGender -> draft.copy(gender = event.gender)
            is OnboardingEvent.SetFitnessLevel -> draft.copy(fitnessLevel = event.level)
            is OnboardingEvent.SetGoal -> draft.copy(goal = event.goal)
            is OnboardingEvent.SetVoiceNote -> draft.copy(voiceNote = event.note)
            is OnboardingEvent.SetAvatar -> draft.copy(avatarId = event.avatarId)
            OnboardingEvent.Complete -> {
                if (draft.age == null || draft.weightKg == null || draft.diet == null ||
                    draft.gender == null || draft.fitnessLevel == null || draft.goal == null ||
                    draft.avatarId == null
                ) {
                    _uiState.value = _uiState.value.copy(error = "Complete required assessment fields")
                    return
                }
                val saveUseCase = saveAssessmentUseCase
                val currentUserId = userId
                if (saveUseCase == null || currentUserId == null) {
                    _uiState.value = _uiState.value.copy(isComplete = true, error = null)
                } else {
                    viewModelScope.launch {
                        try {
                            saveUseCase(currentUserId, draft)
                            _uiState.value = _uiState.value.copy(isComplete = true, error = null)
                        } catch (exception: Exception) {
                            _uiState.value = _uiState.value.copy(
                                error = exception.message ?: "Could not save assessment"
                            )
                        }
                    }
                }
                return
            }
        }
        _uiState.value = _uiState.value.copy(draft = updated, error = null)
    }
}
