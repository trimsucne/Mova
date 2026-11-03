package co.edu.unal.dadm.mova.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import co.edu.unal.dadm.mova.data.entity.WorkoutExerciseEntity

@Dao
interface WorkoutExerciseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(exercises: List<WorkoutExerciseEntity>)
}
