package co.edu.unal.dadm.mova.presentation.viewmodel.workout

import co.edu.unal.dadm.mova.domain.model.Workout
import co.edu.unal.dadm.mova.domain.model.WorkoutExercise

data class WorkoutSessionUiState(
    val loading: Boolean = true,
    val workout: Workout? = null,
    val exerciseIndex: Int = 0,
    /** Sets completados del ejercicio actual. */
    val setsDoneInExercise: Int = 0,
    val completedSets: Int = 0,
    val elapsedSec: Int = 0,
    val resting: Boolean = false,
    val restRemainingSec: Int = 0,
    val paused: Boolean = false,
    val allDone: Boolean = false,
    val saving: Boolean = false,
    /** Se llena cuando la sesion se guardo; la Route navega al resumen. */
    val savedSessionId: String? = null
) {
    val currentExercise: WorkoutExercise? get() = workout?.exercises?.getOrNull(exerciseIndex)
    val nextExercise: WorkoutExercise? get() = workout?.exercises?.getOrNull(exerciseIndex + 1)
    val totalSets: Int get() = workout?.totalSets ?: 0
    val progress: Float get() = if (totalSets == 0) 0f else completedSets.toFloat() / totalSets
}
