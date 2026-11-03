package co.edu.unal.dadm.mova.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * Hidratación diaria, en mililitros.
 *
 * Temporary fake until the Hydration module is implemented: solo lo que usa PR #5.
 */
interface HydrationRepository {
    fun observeTodayWater(userId: String): Flow<Int>
    fun getWaterGoal(userId: String): Flow<Int>
}
