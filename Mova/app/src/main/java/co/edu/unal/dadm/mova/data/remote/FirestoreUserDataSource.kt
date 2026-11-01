package co.edu.unal.dadm.mova.data.remote

import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import co.edu.unal.dadm.mova.domain.model.*

class FirestoreUserDataSource(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    fun observeUser(userId: String) = callbackFlow<UserProfile?> {
        val registration = firestore.collection("users").document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                } else {
                    trySend(snapshot?.toProfile(userId))
                }
            }
        awaitClose { registration.remove() }
    }

    suspend fun updateUser(userId: String, changes: Map<String, Any?>) {
        firestore.collection("users").document(userId).update(changes).await()
    }

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

    private fun com.google.firebase.firestore.DocumentSnapshot.toProfile(userId: String) =
        UserProfile(
            userId = userId,
            email = getString("email").orEmpty(),
            displayName = getString("displayName").orEmpty(),
            avatarId = getString("avatarId"),
            age = getLong("age")?.toInt(),
            weightKg = getDouble("weightKg"),
            diet = getString("diet")?.let { runCatching { Diet.valueOf(it) }.getOrNull() },
            gender = getString("gender")?.let { runCatching { Gender.valueOf(it) }.getOrNull() },
            fitnessLevel = getString("fitnessLevel")?.let { runCatching { FitnessLevel.valueOf(it) }.getOrNull() },
            goal = getString("goal")?.let { runCatching { Goal.valueOf(it) }.getOrNull() },
            upliftScore = getLong("upliftScore")?.toInt() ?: 0
        )
}
