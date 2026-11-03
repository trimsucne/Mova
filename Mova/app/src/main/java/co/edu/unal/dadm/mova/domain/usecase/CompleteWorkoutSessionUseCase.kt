package co.edu.unal.dadm.mova.domain.usecase

import co.edu.unal.dadm.mova.domain.model.Workout
import co.edu.unal.dadm.mova.domain.model.WorkoutSessionLog
import co.edu.unal.dadm.mova.domain.repository.WorkoutRepository
import java.util.UUID

/** Guarda la sesion; las kcal son proporcionales a los sets completados. */
class CompleteWorkoutSessionUseCase(private val repository: WorkoutRepository) {
    suspend operator fun invoke(
        workout: Workout,
        startedAt: Long,
        durationSec: Int,
        completedSets: Int,
        avgBpm: Int? = null
    ): WorkoutSessionLog {
        val total = workout.totalSets.coerceAtLeast(1)
        val progress = completedSets.coerceIn(0, total).toDouble() / total
        val log = WorkoutSessionLog(
            id = UUID.randomUUID().toString(),
            workoutId = workout.id,
            workoutTitle = workout.title,
            startedAt = startedAt,
            durationSec = durationSec,
            kcalBurned = (workout.kcal * progress).toInt(),
            completedSets = completedSets,
            avgBpm = avgBpm
        )
        repository.saveSession(log)
        return log
    }
}
