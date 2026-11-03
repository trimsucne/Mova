package co.edu.unal.dadm.mova.domain.repository

import co.edu.unal.dadm.mova.domain.model.Workout
import co.edu.unal.dadm.mova.domain.model.WorkoutCategory
import co.edu.unal.dadm.mova.domain.model.WorkoutSessionLog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

interface WorkoutRepository {
    fun observeByCategory(category: WorkoutCategory): Flow<List<Workout>>
    fun observeWorkout(id: String): Flow<Workout?>
    fun observeSessions(): Flow<List<WorkoutSessionLog>>
    fun observeSession(id: String): Flow<WorkoutSessionLog?>
    suspend fun saveSession(log: WorkoutSessionLog)

    /** Catalogo completo (lo usa Home y, mas adelante, Busqueda via WorkoutSearchSource). */
    fun observeWorkouts(): Flow<List<Workout>> =
        combine(WorkoutCategory.entries.map { observeByCategory(it) }) { lists -> lists.toList().flatten() }
}
