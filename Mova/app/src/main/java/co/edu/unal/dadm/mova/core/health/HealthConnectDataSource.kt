package co.edu.unal.dadm.mova.core.health

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.BloodPressureRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.OxygenSaturationRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import co.edu.unal.dadm.mova.domain.model.VitalSample
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.Duration
import java.time.Instant

/**
 * Lee las ultimas mediciones de las ultimas 24 h desde Health Connect (sondeo cada 5 s).
 * Emite solo cuando hay frecuencia cardiaca; presion y SpO2 usan 0 si no hay registro.
 */
@RequiresApi(Build.VERSION_CODES.O)
class HealthConnectDataSource(private val client: HealthConnectClient) : HealthDataSource {

    override fun observeVitals(): Flow<VitalSample> = flow {
        while (true) {
            runCatching { readLatest() }.getOrNull()?.let { emit(it) }
            delay(POLL_MS)
        }
    }

    private suspend fun readLatest(): VitalSample? {
        val now = Instant.now()
        val range = TimeRangeFilter.between(now.minus(Duration.ofHours(24)), now)

        val bpm = client.readRecords(
            ReadRecordsRequest(HeartRateRecord::class, range, ascendingOrder = false, pageSize = 5)
        ).records.flatMap { it.samples }.maxByOrNull { it.time }?.beatsPerMinute?.toInt() ?: return null

        val spo2 = client.readRecords(
            ReadRecordsRequest(OxygenSaturationRecord::class, range, ascendingOrder = false, pageSize = 1)
        ).records.firstOrNull()?.percentage?.value?.toInt() ?: 0

        val systolic = client.readRecords(
            ReadRecordsRequest(BloodPressureRecord::class, range, ascendingOrder = false, pageSize = 1)
        ).records.firstOrNull()?.systolic?.inMillimetersOfMercury?.toInt() ?: 0

        return VitalSample(bpm = bpm, systolic = systolic, spo2 = spo2, timestamp = now.toEpochMilli())
    }

    private companion object {
        const val POLL_MS = 5_000L
    }
}
