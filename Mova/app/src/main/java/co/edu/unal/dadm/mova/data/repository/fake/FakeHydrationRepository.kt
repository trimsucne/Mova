package co.edu.unal.dadm.mova.data.repository.fake

import co.edu.unal.dadm.mova.domain.repository.HydrationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/** In-memory HydrationRepository. Temporary fake until the Hydration module is implemented. */
class FakeHydrationRepository : HydrationRepository {
    override fun observeTodayWater(userId: String): Flow<Int> = flowOf(1750)

    override fun getWaterGoal(userId: String): Flow<Int> = flowOf(2500)
}
