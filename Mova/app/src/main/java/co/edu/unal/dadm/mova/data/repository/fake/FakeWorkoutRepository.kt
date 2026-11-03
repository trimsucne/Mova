package co.edu.unal.dadm.mova.data.repository.fake

import co.edu.unal.dadm.mova.data.repository.InMemoryWorkoutRepository
import co.edu.unal.dadm.mova.domain.model.Workout
import co.edu.unal.dadm.mova.domain.model.WorkoutCategory
import co.edu.unal.dadm.mova.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

private val homeWorkouts = listOf(
    Workout("workout_full_body", "Full Body Strength", WorkoutCategory.STRENGTH, 45, caloriesBurned = 320),
    Workout("workout_hiit_20", "HIIT Express", WorkoutCategory.HIIT, 20, caloriesBurned = 250),
    Workout("workout_morning_run", "Morning Run", WorkoutCategory.CARDIO, 30, caloriesBurned = 280),
    Workout("workout_yoga_flow", "Yoga Flow", WorkoutCategory.YOGA, 25, caloriesBurned = 120)
)

/** In-memory WorkoutRepository con los datos de ejemplo de Home. */
class FakeWorkoutRepository : WorkoutRepository by InMemoryWorkoutRepository(homeWorkouts) {
    private val workouts = MutableStateFlow(homeWorkouts)

    override fun observeWorkouts(): Flow<List<Workout>> = workouts.asStateFlow()
}
