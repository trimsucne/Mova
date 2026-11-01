package co.edu.unal.dadm.mova.domain.repository

import co.edu.unal.dadm.mova.domain.model.OnboardingDraft

interface AssessmentRepository {
    suspend fun save(userId: String, draft: OnboardingDraft)
}
