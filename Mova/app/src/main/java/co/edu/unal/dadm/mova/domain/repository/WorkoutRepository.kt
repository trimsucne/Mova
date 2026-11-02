package co.edu.unal.dadm.mova.domain.repository

import co.edu.unal.dadm.mova.domain.model.Workout
import kotlinx.coroutines.flow.Flow

/**
 * Catálogo de entrenamientos (lo usan Home y, más adelante, Búsqueda vía WorkoutSearchSource).
 *
 * Temporary fake until the Workout module is implemented: solo lo que usa PR #5.
 */
interface WorkoutRepository {
    fun observeWorkouts(): Flow<List<Workout>>
}
