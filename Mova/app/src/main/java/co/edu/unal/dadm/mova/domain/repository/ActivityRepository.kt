package co.edu.unal.dadm.mova.domain.repository

import co.edu.unal.dadm.mova.domain.model.ActivityLog
import kotlinx.coroutines.flow.Flow

/**
 * Actividades registradas por el usuario.
 *
 * Temporary fake until the Activity module is implemented: solo lo que usa PR #5.
 */
interface ActivityRepository {
    fun observeTodayActivities(userId: String): Flow<List<ActivityLog>>
}
