package co.edu.unal.dadm.mova.domain.repository

import co.edu.unal.dadm.mova.domain.model.Workout
import co.edu.unal.dadm.mova.domain.model.WorkoutCategory
import co.edu.unal.dadm.mova.domain.model.WorkoutSessionLog
import kotlinx.coroutines.flow.Flow

interface WorkoutRepository {
    fun observeByCategory(category: WorkoutCategory): Flow<List<Workout>>
    fun observeWorkout(id: String): Flow<Workout?>
    fun observeSessions(): Flow<List<WorkoutSessionLog>>
    fun observeSession(id: String): Flow<WorkoutSessionLog?>
    suspend fun saveSession(log: WorkoutSessionLog)
}
