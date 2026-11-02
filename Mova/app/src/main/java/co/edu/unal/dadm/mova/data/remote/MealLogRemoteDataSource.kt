package co.edu.unal.dadm.mova.data.remote

import co.edu.unal.dadm.mova.data.entity.MealLogEntity
import kotlinx.coroutines.flow.Flow

interface MealLogRemoteDataSource {
    suspend fun upsert(log: MealLogEntity)
    suspend fun delete(userId: String, id: String)
    suspend fun fetchAll(userId: String): List<MealLogEntity>
    fun listen(userId: String): Flow<List<RemoteChange>>
}

sealed interface RemoteChange {
    data class Upserted(val log: MealLogEntity) : RemoteChange
    data class Removed(val id: String) : RemoteChange
}
