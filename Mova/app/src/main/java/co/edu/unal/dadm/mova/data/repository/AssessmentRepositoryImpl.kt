package co.edu.unal.dadm.mova.data.repository

import co.edu.unal.dadm.mova.data.remote.FirestoreAssessmentDataSource
import co.edu.unal.dadm.mova.domain.model.OnboardingDraft
import co.edu.unal.dadm.mova.domain.repository.AssessmentRepository

class AssessmentRepositoryImpl(
    private val dataSource: FirestoreAssessmentDataSource = FirestoreAssessmentDataSource()
) : AssessmentRepository {
    override suspend fun save(userId: String, draft: OnboardingDraft) {
        dataSource.save(userId, draft)
    }
}
