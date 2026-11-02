package co.edu.unal.dadm.mova.data.remote

import co.edu.unal.dadm.mova.data.entity.MealLogEntity
import co.edu.unal.dadm.mova.data.entity.SyncState
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date

/** users/{uid}/meal_logs/{id}; mismos campos que la tabla salvo syncState (spec §5). */
class FirestoreMealLogDataSource(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : MealLogRemoteDataSource {

    private fun collection(userId: String) =
        firestore.collection("users").document(userId).collection("meal_logs")

    override suspend fun upsert(log: MealLogEntity) {
        collection(log.userId).document(log.id).set(log.toDocument()).await()
    }

    override suspend fun delete(userId: String, id: String) {
        collection(userId).document(id).delete().await()
    }

    override suspend fun fetchAll(userId: String): List<MealLogEntity> =
        collection(userId).get().await().documents.mapNotNull { it.toMealLogEntity() }

    override fun listen(userId: String): Flow<List<RemoteChange>> = callbackFlow {
        val registration = collection(userId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val changes = snapshot?.documentChanges.orEmpty().mapNotNull { change ->
                when (change.type) {
                    DocumentChange.Type.REMOVED -> RemoteChange.Removed(change.document.id)
                    else -> change.document.toMealLogEntity()?.let { RemoteChange.Upserted(it) }
                }
            }
            if (changes.isNotEmpty()) trySend(changes)
        }
        awaitClose { registration.remove() }
    }
}

private fun MealLogEntity.toDocument(): Map<String, Any?> = mapOf(
    "id" to id,
    "userId" to userId,
    "date" to date,
    "mealType" to mealType,
    "source" to source,
    "mealId" to mealId,
    "name" to name,
    "grams" to grams,
    "proteinG" to proteinG,
    "carbsG" to carbsG,
    "fatG" to fatG,
    "kcal" to kcal,
    "createdAt" to Timestamp(Date(createdAt)),
    "updatedAt" to Timestamp(Date(updatedAt)),
    "deleted" to deleted
)

private fun DocumentSnapshot.toMealLogEntity(): MealLogEntity? = runCatching {
    MealLogEntity(
        id = id,
        userId = getString("userId")!!,
        date = getString("date")!!,
        mealType = getString("mealType")!!,
        source = getString("source")!!,
        mealId = getString("mealId"),
        name = getString("name")!!,
        grams = getLong("grams")?.toInt(),
        proteinG = getDouble("proteinG") ?: 0.0,
        carbsG = getDouble("carbsG") ?: 0.0,
        fatG = getDouble("fatG") ?: 0.0,
        kcal = getLong("kcal")!!.toInt(),
        createdAt = getTimestamp("createdAt")!!.toDate().time,
        updatedAt = getTimestamp("updatedAt")!!.toDate().time,
        deleted = getBoolean("deleted") ?: false,
        syncState = SyncState.SYNCED.name
    )
}.getOrNull()
