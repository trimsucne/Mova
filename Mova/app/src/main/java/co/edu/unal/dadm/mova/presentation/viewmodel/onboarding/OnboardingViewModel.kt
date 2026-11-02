package co.edu.unal.dadm.mova.presentation.viewmodel.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.unal.dadm.mova.domain.model.Diet
import co.edu.unal.dadm.mova.domain.model.FitnessLevel
import co.edu.unal.dadm.mova.domain.model.Gender
import co.edu.unal.dadm.mova.domain.model.Goal
import co.edu.unal.dadm.mova.domain.usecase.SaveAssessmentUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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
                val completedDraft = draft.copy(
                    age = draft.age ?: 19,
                    weightKg = draft.weightKg ?: 62.0,
                    diet = draft.diet ?: Diet.VEGAN,
                    gender = draft.gender ?: Gender.MALE,
                    fitnessLevel = draft.fitnessLevel ?: FitnessLevel.INTERMEDIATE,
                    goal = draft.goal ?: Goal.AI_COACH,
                    avatarId = draft.avatarId ?: "avatar_2"
                )
                val saveUseCase = saveAssessmentUseCase
                val currentUserId = userId
                if (saveUseCase == null || currentUserId == null) {
                    _uiState.value = _uiState.value.copy(draft = completedDraft, isComplete = true, error = null)
                } else {
                    viewModelScope.launch {
                        try {
                            saveUseCase(currentUserId, completedDraft)
                            _uiState.value = _uiState.value.copy(draft = completedDraft, isComplete = true, error = null)
                        } catch (_: Exception) {
                            _uiState.value = _uiState.value.copy(draft = completedDraft, isComplete = true, error = null)
                        }
                    }
                }
                return
            }
        }
        _uiState.value = _uiState.value.copy(draft = updated, error = null)
    }
}
