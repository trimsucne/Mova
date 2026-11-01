package co.edu.unal.dadm.mova.presentation.viewmodel.onboarding

import co.edu.unal.dadm.mova.domain.model.OnboardingDraft

data class OnboardingUiState(
    val draft: OnboardingDraft = OnboardingDraft(),
    val isComplete: Boolean = false,
    val error: String? = null
)
