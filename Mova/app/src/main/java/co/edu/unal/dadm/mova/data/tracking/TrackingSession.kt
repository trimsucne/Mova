package co.edu.unal.dadm.mova.data.tracking

import co.edu.unal.dadm.mova.core.common.GeoUtils
import co.edu.unal.dadm.mova.domain.model.ActivityType
import co.edu.unal.dadm.mova.domain.model.RoutePoint
import co.edu.unal.dadm.mova.domain.model.TrackingState
import co.edu.unal.dadm.mova.domain.model.TrackingStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Logica pura del tracking (sin Android): acumula distancia filtrando lecturas GPS malas
 * y lleva el cronometro. El servicio en primer plano la alimenta; la UI solo observa.
 */
class TrackingSession(private val clock: () -> Long = System::currentTimeMillis) {
    private val _state = MutableStateFlow(TrackingState())
    val state: StateFlow<TrackingState> = _state.asStateFlow()

    fun start(type: ActivityType) {
        _state.value = TrackingState(
            status = TrackingStatus.Running,
            type = type,
            startedAt = clock()
        )
    }

    fun pause() = _state.update {
        if (it.status == TrackingStatus.Running) it.copy(status = TrackingStatus.Paused) else it
    }

    fun resume() = _state.update {
        if (it.status == TrackingStatus.Paused) it.copy(status = TrackingStatus.Running) else it
    }

    /** Llamar cada segundo. */
    fun tick() = _state.update {
        if (it.status == TrackingStatus.Running) it.copy(elapsedSec = it.elapsedSec + 1) else it
    }

    /** Procesa una lectura GPS; devuelve true si fue aceptada. */
    fun onLocation(lat: Double, lng: Double, timestamp: Long, accuracyMeters: Float?): Boolean {
        var accepted = false
        _state.update { s ->
            if (s.status != TrackingStatus.Running) return@update s
            if (accuracyMeters != null && accuracyMeters > MAX_ACCURACY_M) return@update s
            val last = s.route.lastOrNull()
            if (last == null) {
                accepted = true
                return@update s.copy(route = listOf(RoutePoint(lat, lng, timestamp)))
            }
            val d = GeoUtils.distanceMeters(last.lat, last.lng, lat, lng)
            val dtSec = ((timestamp - last.timestamp) / 1000.0).coerceAtLeast(0.001)
            if (d < MIN_STEP_M || d / dtSec > MAX_SPEED_MPS) return@update s
            accepted = true
            s.copy(
                distanceMeters = s.distanceMeters + d,
                route = s.route + RoutePoint(lat, lng, timestamp)
            )
        }
        return accepted
    }

    /** Termina la sesion y la reinicia; devuelve el estado final o null si no habia sesion. */
    fun finish(): TrackingState? {
        val final = _state.value
        if (final.status == TrackingStatus.Idle) return null
        _state.value = TrackingState()
        return final
    }

    companion object {
        const val MAX_ACCURACY_M = 25f
        const val MIN_STEP_M = 2.0
        const val MAX_SPEED_MPS = 15.0
    }
}
