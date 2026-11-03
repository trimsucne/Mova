package co.edu.unal.dadm.mova.data.repository

import co.edu.unal.dadm.mova.domain.model.Workout
import co.edu.unal.dadm.mova.domain.model.WorkoutCategory
import co.edu.unal.dadm.mova.domain.model.WorkoutSeed
import co.edu.unal.dadm.mova.domain.model.WorkoutSessionLog
import co.edu.unal.dadm.mova.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/**
 * Implementacion en memoria con catalogo semilla. Se reemplazara por Room
 * (WorkoutDao/WorkoutSessionDao) sin tocar domain ni presentation.
 */
class InMemoryWorkoutRepository(
    seed: List<Workout> = WorkoutSeed.workouts
) : WorkoutRepository {
    private val workouts = MutableStateFlow(seed)
    private val sessions = MutableStateFlow<List<WorkoutSessionLog>>(emptyList())

    override fun observeByCategory(category: WorkoutCategory): Flow<List<Workout>> =
        workouts.map { list -> list.filter { it.category == category } }

    override fun observeWorkout(id: String): Flow<Workout?> =
        workouts.map { list -> list.firstOrNull { it.id == id } }

    override fun observeSessions(): Flow<List<WorkoutSessionLog>> = sessions

    override fun observeSession(id: String): Flow<WorkoutSessionLog?> =
        sessions.map { list -> list.firstOrNull { it.id == id } }

    override suspend fun saveSession(log: WorkoutSessionLog) {
        sessions.update { listOf(log) + it }
    }
}
