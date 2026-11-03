package co.edu.unal.dadm.mova.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Sin FK a workouts: el historial se conserva aunque el catalogo cambie. */
@Entity(tableName = "workout_sessions")
data class WorkoutSessionEntity(
    @PrimaryKey val id: String,
    val workoutId: String,
    val workoutTitle: String,
    val startedAt: Long,
    val durationSec: Int,
    val kcalBurned: Int,
    val completedSets: Int,
    val avgBpm: Int?
)
