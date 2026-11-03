package co.edu.unal.dadm.mova.domain.repository

import co.edu.unal.dadm.mova.domain.model.RoutePlan

interface DirectionsRepository {
    /** Ruta a pie por calles, o null si no hay red / servicio / ruta (la UI cae a linea recta). */
    suspend fun walkingRoute(fromLat: Double, fromLng: Double, toLat: Double, toLng: Double): RoutePlan?
}
