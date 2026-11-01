package co.edu.unal.dadm.mova.domain.usecase

import co.edu.unal.dadm.mova.domain.model.OnboardingDraft
import co.edu.unal.dadm.mova.domain.repository.AssessmentRepository

class SaveAssessmentUseCase(private val repository: AssessmentRepository) {
    suspend operator fun invoke(userId: String, draft: OnboardingDraft) {
        repository.save(userId, draft)
    }
}
