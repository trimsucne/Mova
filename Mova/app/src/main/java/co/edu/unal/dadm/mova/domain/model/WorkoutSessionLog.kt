package co.edu.unal.dadm.mova.domain.model

data class WorkoutSessionLog(
    val id: String,
    val workoutId: String,
    val workoutTitle: String,
    val startedAt: Long,
    val durationSec: Int,
    val kcalBurned: Int,
    val completedSets: Int,
    val avgBpm: Int? = null
) {
    val minutes: Int get() = (durationSec + 59) / 60
}
