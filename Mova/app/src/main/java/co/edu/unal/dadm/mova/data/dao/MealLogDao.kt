package co.edu.unal.dadm.mova.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import co.edu.unal.dadm.mova.data.entity.MealLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MealLogDao {
    @Query("SELECT * FROM meal_logs WHERE userId = :userId AND date = :date AND deleted = 0 ORDER BY createdAt")
    fun observeByDate(userId: String, date: String): Flow<List<MealLogEntity>>

    @Query("SELECT * FROM meal_logs WHERE userId = :userId AND date BETWEEN :from AND :to AND deleted = 0 ORDER BY date, createdAt")
    fun observeRange(userId: String, from: String, to: String): Flow<List<MealLogEntity>>

    @Query("SELECT * FROM meal_logs WHERE id = :id")
    suspend fun getById(id: String): MealLogEntity?

    @Upsert
    suspend fun upsert(log: MealLogEntity)

    @Upsert
    suspend fun upsertAll(logs: List<MealLogEntity>)

    @Query("SELECT * FROM meal_logs WHERE userId = :userId AND syncState = 'PENDING'")
    suspend fun getPending(userId: String): List<MealLogEntity>

    /** Solo marca SYNCED si nadie modificó el registro mientras se subía. */
    @Query("UPDATE meal_logs SET syncState = 'SYNCED' WHERE id = :id AND updatedAt = :updatedAt")
    suspend fun markSynced(id: String, updatedAt: Long)

    @Query("DELETE FROM meal_logs WHERE id = :id")
    suspend fun hardDelete(id: String)

    @Query("DELETE FROM meal_logs WHERE userId = :userId AND syncState = 'SYNCED'")
    suspend fun deleteSyncedForUser(userId: String)

    @Query("SELECT COUNT(*) FROM meal_logs WHERE userId = :userId")
    suspend fun countForUser(userId: String): Int
}
