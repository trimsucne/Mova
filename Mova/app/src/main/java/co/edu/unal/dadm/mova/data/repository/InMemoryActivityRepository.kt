package co.edu.unal.dadm.mova.data.repository

import co.edu.unal.dadm.mova.domain.model.ActivityLog
import co.edu.unal.dadm.mova.domain.model.ActivityType
import co.edu.unal.dadm.mova.domain.repository.ActivityRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/** Implementacion en memoria; migrara a Room (ActivityLogDao) sin cambiar la interfaz. */
class InMemoryActivityRepository : ActivityRepository {
    private val logs = MutableStateFlow<List<ActivityLog>>(emptyList())

    override fun observeAll(): Flow<List<ActivityLog>> =
        logs.map { list -> list.sortedByDescending { it.startedAt } }

    override fun observe(id: String): Flow<ActivityLog?> =
        logs.map { list -> list.firstOrNull { it.id == id } }

    override fun observeHoursByType(): Flow<Map<ActivityType, Double>> =
        logs.map { list ->
            list.groupBy { it.type }.mapValues { (_, items) -> items.sumOf { it.hours } }
        }

    override suspend fun save(log: ActivityLog) {
        logs.update { it + log }
    }
}
