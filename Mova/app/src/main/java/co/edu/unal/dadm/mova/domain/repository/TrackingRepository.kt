package co.edu.unal.dadm.mova.domain.repository

import co.edu.unal.dadm.mova.domain.model.ActivityType
import co.edu.unal.dadm.mova.domain.model.TrackingState
import kotlinx.coroutines.flow.StateFlow

interface TrackingRepository {
    val state: StateFlow<TrackingState>
    fun start(type: ActivityType)
    fun pause()
    fun resume()

    /** Detiene el tracking y devuelve el estado final (o null si no habia sesion). */
    fun stop(): TrackingState?
}
