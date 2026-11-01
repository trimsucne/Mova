package co.edu.unal.dadm.mova.presentation.viewmodel.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import co.edu.unal.dadm.mova.domain.usecase.SaveAssessmentUseCase

class OnboardingViewModelFactory(
    private val saveAssessmentUseCase: SaveAssessmentUseCase,
    private val userId: String
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return OnboardingViewModel(saveAssessmentUseCase, userId) as T
    }
}
