package co.edu.unal.dadm.mova.data.remote

import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestoreUserDataSource(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    suspend fun ensureUserDocument(user: FirebaseUser) {
        val reference = firestore.collection("users").document(user.uid)
        val existing = reference.get().await()
        if (!existing.exists()) {
            reference.set(
                mapOf(
                    "email" to user.email.orEmpty(),
                    "displayName" to user.displayName,
                    "photoUrl" to user.photoUrl?.toString(),
                    "provider" to user.providerData
                        .firstOrNull { it.providerId != "firebase" }
                        ?.providerId,
                    "onboardingCompleted" to false
                )
            ).await()
        }
    }
}
