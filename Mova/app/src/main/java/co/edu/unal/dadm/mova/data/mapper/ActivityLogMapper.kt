package co.edu.unal.dadm.mova.data.mapper

import co.edu.unal.dadm.mova.data.entity.ActivityLogEntity
import co.edu.unal.dadm.mova.domain.model.ActivityLog
import co.edu.unal.dadm.mova.domain.model.ActivityType
import co.edu.unal.dadm.mova.domain.model.RoutePoint

internal fun encodeRoute(route: List<RoutePoint>): String =
    route.joinToString(";") { "${it.lat},${it.lng},${it.timestamp}" }

internal fun decodeRoute(encoded: String): List<RoutePoint> =
    if (encoded.isEmpty()) emptyList()
    else encoded.split(";").mapNotNull { chunk ->
        val p = chunk.split(",")
        if (p.size != 3) return@mapNotNull null
        val lat = p[0].toDoubleOrNull() ?: return@mapNotNull null
        val lng = p[1].toDoubleOrNull() ?: return@mapNotNull null
        val ts = p[2].toLongOrNull() ?: return@mapNotNull null
        RoutePoint(lat, lng, ts)
    }

fun ActivityLogEntity.toDomain() = ActivityLog(
    id = id,
    type = ActivityType.fromId(type) ?: ActivityType.Running,
    startedAt = startedAt,
    durationSec = durationSec,
    distanceMeters = distanceMeters,
    kcal = kcal,
    avgBpm = avgBpm,
    route = decodeRoute(routeEncoded)
)

fun ActivityLog.toEntity() = ActivityLogEntity(
    id = id, type = type.id, startedAt = startedAt, durationSec = durationSec,
    distanceMeters = distanceMeters, kcal = kcal, avgBpm = avgBpm, routeEncoded = encodeRoute(route)
)
