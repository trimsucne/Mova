package co.edu.unal.dadm.mova.data.remote

import co.edu.unal.dadm.mova.domain.model.RoutePlan
import co.edu.unal.dadm.mova.domain.repository.DirectionsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

/**
 * Rutas a pie con OSRM (OpenStreetMap), sin API key ni facturacion.
 * Por defecto usa el servidor publico de FOSSGIS con perfil peatonal (su URL usa "driving" como nombre
 * de perfil aunque el servidor sea de a pie). Es un servicio comunitario sin SLA: cualquier fallo devuelve null.
 */
class OsrmDirectionsRepository(
    private val baseUrl: String = "https://routing.openstreetmap.de/routed-foot/route/v1/driving",
    private val timeoutMs: Int = 8_000
) : DirectionsRepository {

    override suspend fun walkingRoute(
        fromLat: Double, fromLng: Double, toLat: Double, toLng: Double
    ): RoutePlan? = withContext(Dispatchers.IO) {
        runCatching {
            val coords = String.format(Locale.US, "%.6f,%.6f;%.6f,%.6f", fromLng, fromLat, toLng, toLat)
            val url = URL("$baseUrl/$coords?overview=full&geometries=geojson&steps=true")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = timeoutMs
                readTimeout = timeoutMs
                setRequestProperty("User-Agent", "Mova/1.0 (Android; university project)")
            }
            try {
                if (conn.responseCode != HttpURLConnection.HTTP_OK) return@runCatching null
                OsrmRouteParser.parse(conn.inputStream.bufferedReader().use { it.readText() })
            } finally {
                conn.disconnect()
            }
        }.getOrNull()
    }
}
