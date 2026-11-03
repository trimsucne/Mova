package co.edu.unal.dadm.mova.core.health

import android.content.Context
import android.os.Build
import androidx.health.connect.client.HealthConnectClient
import co.edu.unal.dadm.mova.domain.model.VitalSample
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

/**
 * Usa Health Connect si esta disponible y con permisos; si no (emulador, API < 26,
 * permiso denegado) cae al simulador [FakeHealthDataSource]. La decision se toma en cada
 * suscripcion, asi que tras conceder permisos basta con volver a observar.
 */
class AdaptiveHealthDataSource(
    private val context: Context,
    private val fake: HealthDataSource = FakeHealthDataSource()
) : HealthDataSource {
    override fun observeVitals(): Flow<VitalSample> = flow {
        emitAll(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && HealthPermissions.hasAll(context)) {
                HealthConnectDataSource(HealthConnectClient.getOrCreate(context)).observeVitals()
            } else {
                fake.observeVitals()
            }
        )
    }
}
