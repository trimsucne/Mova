package co.edu.unal.dadm.mova.core.health

import co.edu.unal.dadm.mova.domain.model.VitalSample
import kotlinx.coroutines.flow.Flow

/** Fuente de signos vitales. Implementaciones: Fake (hoy) y Health Connect (pendiente). */
interface HealthDataSource {
    fun observeVitals(): Flow<VitalSample>
}
