package co.edu.unal.dadm.mova.core.health

import co.edu.unal.dadm.mova.domain.model.VitalSample
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.random.Random

/** Genera lecturas simuladas (emulador o permiso denegado). */
class FakeHealthDataSource : HealthDataSource {
    override fun observeVitals(): Flow<VitalSample> = flow {
        var bpm = 78
        while (true) {
            bpm = (bpm + Random.nextInt(-3, 4)).coerceIn(60, 150)
            emit(
                VitalSample(
                    bpm = bpm,
                    systolic = 110 + Random.nextInt(-4, 5),
                    spo2 = 96 + Random.nextInt(0, 4),
                    timestamp = System.currentTimeMillis()
                )
            )
            delay(2_000)
        }
    }
}
