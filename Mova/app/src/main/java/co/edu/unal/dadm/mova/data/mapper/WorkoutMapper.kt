package co.edu.unal.dadm.mova.data.mapper

import co.edu.unal.dadm.mova.data.dao.WorkoutWithExercises
import co.edu.unal.dadm.mova.data.entity.WorkoutEntity
import co.edu.unal.dadm.mova.data.entity.WorkoutExerciseEntity
import co.edu.unal.dadm.mova.domain.model.Workout
import co.edu.unal.dadm.mova.domain.model.WorkoutCategory
import co.edu.unal.dadm.mova.domain.model.WorkoutExercise

fun WorkoutWithExercises.toDomain(): Workout = Workout(
    id = workout.id,
    name = workout.title,
    category = WorkoutCategory.fromId(workout.category),
    durationMin = workout.durationMin,
    imageRes = workout.imageKey,
    caloriesBurned = workout.kcal,
    trainer = workout.trainer,
    sets = workout.sets,
    reps = workout.reps,
    exercises = exercises.sortedBy { it.position }.map { it.toDomain() }
)

fun Workout.toEntities(): Pair<WorkoutEntity, List<WorkoutExerciseEntity>> = WorkoutEntity(
    id = id, title = title, category = category.id, trainer = trainer,
    durationMin = durationMin, kcal = kcal, sets = sets, reps = reps, imageKey = imageKey
) to exercises.mapIndexed { index, ex -> ex.toEntity(workoutId = id, position = index) }
