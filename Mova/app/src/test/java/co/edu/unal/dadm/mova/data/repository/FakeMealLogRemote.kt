package co.edu.unal.dadm.mova.data.repository

import co.edu.unal.dadm.mova.data.entity.MealLogEntity
import co.edu.unal.dadm.mova.data.remote.MealLogRemoteDataSource
import co.edu.unal.dadm.mova.data.remote.RemoteChange
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

class FakeMealLogRemote : MealLogRemoteDataSource {
    val docs = mutableMapOf<String, MealLogEntity>()
    var offline = false
    val changes = MutableSharedFlow<List<RemoteChange>>(extraBufferCapacity = 8)

    private fun failIfOffline() {
        if (offline) throw IllegalStateException("offline")
    }

    override suspend fun upsert(log: MealLogEntity) { failIfOffline(); docs[log.id] = log }
    override suspend fun delete(userId: String, id: String) { failIfOffline(); docs.remove(id) }
    override suspend fun fetchAll(userId: String): List<MealLogEntity> {
        failIfOffline()
        return docs.values.filter { it.userId == userId }
    }
    override fun listen(userId: String): Flow<List<RemoteChange>> = changes
}
