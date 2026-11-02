package co.edu.unal.dadm.mova.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.time.LocalDate

/** Puerto hacia Actividad (D3). Mientras Actividad no lo exponga, se usa NONE y la meta es fija. */
fun interface BurnedKcalProvider {
    fun observeBurnedKcal(userId: String, date: LocalDate): Flow<Int>

    companion object {
        val NONE = BurnedKcalProvider { _, _ -> flowOf(0) }
    }
}
