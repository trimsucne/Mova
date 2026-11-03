package co.edu.unal.dadm.mova.domain.usecase

import co.edu.unal.dadm.mova.domain.model.ActivityLog
import co.edu.unal.dadm.mova.domain.model.ActivityType
import co.edu.unal.dadm.mova.domain.model.RoutePoint
import co.edu.unal.dadm.mova.domain.repository.ActivityRepository
import java.util.UUID

/** Registra una actividad calculando kcal = MET x peso(kg) x horas. */
class LogActivityUseCase(private val repository: ActivityRepository) {
    suspend operator fun invoke(
        type: ActivityType,
        startedAt: Long,
        durationSec: Int,
        distanceMeters: Double,
        route: List<RoutePoint> = emptyList(),
        weightKg: Double = DEFAULT_WEIGHT_KG,
        avgBpm: Int? = null
    ): ActivityLog {
        val kcal = (type.met * weightKg * (durationSec / 3600.0)).toInt()
        val log = ActivityLog(
            id = UUID.randomUUID().toString(),
            type = type,
            startedAt = startedAt,
            durationSec = durationSec,
            distanceMeters = distanceMeters,
            kcal = kcal,
            avgBpm = avgBpm,
            route = route
        )
        repository.save(log)
        return log
    }

    companion object {
        const val DEFAULT_WEIGHT_KG = 70.0
    }
}
