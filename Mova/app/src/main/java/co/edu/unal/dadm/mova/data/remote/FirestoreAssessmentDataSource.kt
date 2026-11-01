package co.edu.unal.dadm.mova.data.remote

import co.edu.unal.dadm.mova.domain.model.OnboardingDraft
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestoreAssessmentDataSource(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    suspend fun save(userId: String, draft: OnboardingDraft) {
        val values = mapOf(
            "age" to draft.age,
            "weightKg" to draft.weightKg,
            "diet" to draft.diet?.name,
            "gender" to draft.gender?.name,
            "fitnessLevel" to draft.fitnessLevel?.name,
            "goal" to draft.goal?.name,
            "voiceNote" to draft.voiceNote,
            "avatarId" to draft.avatarId,
            "completed" to true
        )
        firestore.collection("users").document(userId)
            .collection("assessment").document("main")
            .set(values).await()
        firestore.collection("users").document(userId)
            .set(values + ("onboardingCompleted" to true), com.google.firebase.firestore.SetOptions.merge())
            .await()
    }
}
