package co.edu.unal.dadm.mova.data.repository

import co.edu.unal.dadm.mova.data.dao.WorkoutDao
import co.edu.unal.dadm.mova.data.dao.WorkoutSessionDao
import co.edu.unal.dadm.mova.data.mapper.toDomain
import co.edu.unal.dadm.mova.data.mapper.toEntity
import co.edu.unal.dadm.mova.domain.model.Workout
import co.edu.unal.dadm.mova.domain.model.WorkoutCategory
import co.edu.unal.dadm.mova.domain.model.WorkoutSessionLog
import co.edu.unal.dadm.mova.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Implementacion persistente (Room). */
class WorkoutRepositoryImpl(
    private val workoutDao: WorkoutDao,
    private val sessionDao: WorkoutSessionDao
) : WorkoutRepository {
    override fun observeByCategory(category: WorkoutCategory): Flow<List<Workout>> =
        workoutDao.observeByCategory(category.id).map { rows -> rows.map { it.toDomain() } }

    override fun observeWorkout(id: String): Flow<Workout?> =
        workoutDao.observeById(id).map { it?.toDomain() }

    override fun observeSessions(): Flow<List<WorkoutSessionLog>> =
        sessionDao.observeAll().map { rows -> rows.map { it.toDomain() } }

    override fun observeSession(id: String): Flow<WorkoutSessionLog?> =
        sessionDao.observeById(id).map { it?.toDomain() }

    override suspend fun saveSession(log: WorkoutSessionLog) = sessionDao.insert(log.toEntity())
}
