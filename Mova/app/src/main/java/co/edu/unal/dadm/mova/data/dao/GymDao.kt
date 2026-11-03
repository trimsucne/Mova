package co.edu.unal.dadm.mova.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import co.edu.unal.dadm.mova.data.entity.GymEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GymDao {
    @Query("SELECT * FROM gyms ORDER BY name")
    fun observeAll(): Flow<List<GymEntity>>

    @Query("SELECT * FROM gyms WHERE id = :id")
    fun observeById(id: String): Flow<GymEntity?>

    @Query("SELECT COUNT(*) FROM gyms")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(gyms: List<GymEntity>)
}
