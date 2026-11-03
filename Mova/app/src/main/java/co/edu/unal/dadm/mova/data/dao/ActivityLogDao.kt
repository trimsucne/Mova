package co.edu.unal.dadm.mova.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import co.edu.unal.dadm.mova.data.entity.ActivityLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityLogDao {
    @Query("SELECT * FROM activity_logs ORDER BY startedAt DESC")
    fun observeAll(): Flow<List<ActivityLogEntity>>

    @Query("SELECT * FROM activity_logs WHERE id = :id")
    fun observeById(id: String): Flow<ActivityLogEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: ActivityLogEntity)
}
