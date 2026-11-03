package co.edu.unal.dadm.mova.domain.repository

import co.edu.unal.dadm.mova.domain.model.ActivityLog
import co.edu.unal.dadm.mova.domain.model.ActivityType
import kotlinx.coroutines.flow.Flow

interface ActivityRepository {
    fun observeAll(): Flow<List<ActivityLog>>
    fun observe(id: String): Flow<ActivityLog?>

    /** Horas acumuladas por tipo de actividad (pantalla Activity Status). */
    fun observeHoursByType(): Flow<Map<ActivityType, Double>>
    suspend fun save(log: ActivityLog)
}
