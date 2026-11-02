package co.edu.unal.dadm.mova.data.repository.fake

import co.edu.unal.dadm.mova.domain.model.ActivityLog
import co.edu.unal.dadm.mova.domain.repository.ActivityRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/** In-memory ActivityRepository. Temporary fake until the Activity module is implemented. */
class FakeActivityRepository : ActivityRepository {
    private val today = listOf(
        ActivityLog("activity_walk", "Morning Walk", 35, 2.8, 140),
        ActivityLog("activity_cycling", "Cycling", 40, 12.5, 360)
    )

    override fun observeTodayActivities(userId: String): Flow<List<ActivityLog>> = flowOf(today)
}
