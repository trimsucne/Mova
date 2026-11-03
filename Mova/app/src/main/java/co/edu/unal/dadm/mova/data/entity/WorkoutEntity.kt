package co.edu.unal.dadm.mova.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workouts")
data class WorkoutEntity(
    @PrimaryKey val id: String,
    val title: String,
    val category: String,
    val trainer: String?,
    val durationMin: Int,
    val kcal: Int,
    val sets: Int,
    val reps: Int,
    val imageKey: String
)
