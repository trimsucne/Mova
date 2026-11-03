package co.edu.unal.dadm.mova.data.repository

import co.edu.unal.dadm.mova.data.dao.ActivityLogDao
import co.edu.unal.dadm.mova.data.mapper.toDomain
import co.edu.unal.dadm.mova.data.mapper.toEntity
import co.edu.unal.dadm.mova.domain.model.ActivityLog
import co.edu.unal.dadm.mova.domain.model.ActivityType
import co.edu.unal.dadm.mova.domain.repository.ActivityRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Implementacion persistente (Room). */
class ActivityRepositoryImpl(private val dao: ActivityLogDao) : ActivityRepository {
    override fun observeAll(): Flow<List<ActivityLog>> =
        dao.observeAll().map { rows -> rows.map { it.toDomain() } }

    override fun observe(id: String): Flow<ActivityLog?> =
        dao.observeById(id).map { it?.toDomain() }

    override fun observeHoursByType(): Flow<Map<ActivityType, Double>> =
        observeAll().map { list ->
            list.groupBy { it.type }.mapValues { (_, items) -> items.sumOf { it.hours } }
        }

    override suspend fun save(log: ActivityLog) = dao.insert(log.toEntity())
}
