package co.edu.unal.dadm.mova.data.dao

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import co.edu.unal.dadm.mova.data.entity.WorkoutEntity
import co.edu.unal.dadm.mova.data.entity.WorkoutExerciseEntity
import kotlinx.coroutines.flow.Flow

data class WorkoutWithExercises(
    @Embedded val workout: WorkoutEntity,
    @Relation(parentColumn = "id", entityColumn = "workoutId")
    val exercises: List<WorkoutExerciseEntity>
)

@Dao
interface WorkoutDao {
    @Transaction
    @Query("SELECT * FROM workouts WHERE category = :category ORDER BY rowid")
    fun observeByCategory(category: String): Flow<List<WorkoutWithExercises>>

    @Transaction
    @Query("SELECT * FROM workouts WHERE id = :id")
    fun observeById(id: String): Flow<WorkoutWithExercises?>

    @Query("SELECT COUNT(*) FROM workouts")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(workouts: List<WorkoutEntity>)
}
