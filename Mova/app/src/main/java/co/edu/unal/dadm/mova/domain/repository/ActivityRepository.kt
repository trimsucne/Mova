package co.edu.unal.dadm.mova.domain.repository

import co.edu.unal.dadm.mova.domain.model.ActivityLog
import co.edu.unal.dadm.mova.domain.model.ActivityType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar

interface ActivityRepository {
    fun observeAll(): Flow<List<ActivityLog>>
    fun observe(id: String): Flow<ActivityLog?>

    /** Horas acumuladas por tipo de actividad (pantalla Activity Status). */
    fun observeHoursByType(): Flow<Map<ActivityType, Double>>
    suspend fun save(log: ActivityLog)

    /** Actividades de hoy (tarjeta de actividades de Home). Los registros son locales, [userId] no filtra. */
    fun observeTodayActivities(userId: String): Flow<List<ActivityLog>> =
        observeAll().map { logs ->
            val startOfDay = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            logs.filter { it.startedAt >= startOfDay }
        }
}
