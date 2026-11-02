package co.edu.unal.dadm.mova.data.repository

import co.edu.unal.dadm.mova.data.dao.MealLogDao
import co.edu.unal.dadm.mova.data.entity.MealLogEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeMealLogDao : MealLogDao {
    val rows = MutableStateFlow<Map<String, MealLogEntity>>(emptyMap())

    override fun observeByDate(userId: String, date: String): Flow<List<MealLogEntity>> =
        rows.map { all -> all.values.filter { it.userId == userId && it.date == date && !it.deleted }.sortedBy { it.createdAt } }

    override fun observeRange(userId: String, from: String, to: String): Flow<List<MealLogEntity>> =
        rows.map { all ->
            all.values.filter { it.userId == userId && it.date in from..to && !it.deleted }
                .sortedWith(compareBy({ it.date }, { it.createdAt }))
        }

    override suspend fun getById(id: String) = rows.value[id]
    override suspend fun upsert(log: MealLogEntity) { rows.value = rows.value + (log.id to log) }
    override suspend fun upsertAll(logs: List<MealLogEntity>) { rows.value = rows.value + logs.associateBy { it.id } }
    override suspend fun getPending(userId: String) =
        rows.value.values.filter { it.userId == userId && it.syncState == "PENDING" }
    override suspend fun markSynced(id: String, updatedAt: Long) {
        val row = rows.value[id] ?: return
        if (row.updatedAt == updatedAt) upsert(row.copy(syncState = "SYNCED"))
    }
    override suspend fun hardDelete(id: String) { rows.value = rows.value - id }
    override suspend fun deleteSyncedForUser(userId: String) {
        rows.value = rows.value.filterValues { !(it.userId == userId && it.syncState == "SYNCED") }
    }
    override suspend fun countForUser(userId: String) = rows.value.values.count { it.userId == userId }
}
