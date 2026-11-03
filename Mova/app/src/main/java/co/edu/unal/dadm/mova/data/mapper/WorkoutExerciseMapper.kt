package co.edu.unal.dadm.mova.data.mapper

import co.edu.unal.dadm.mova.data.entity.WorkoutExerciseEntity
import co.edu.unal.dadm.mova.domain.model.WorkoutExercise

fun WorkoutExerciseEntity.toDomain() = WorkoutExercise(id, name, sets, reps, restSeconds)

fun WorkoutExercise.toEntity(workoutId: String, position: Int) =
    WorkoutExerciseEntity(id, workoutId, position, name, sets, reps, restSeconds)
