package co.edu.unal.dadm.mova.domain.model

/**
 * Entrenamiento. Los seis primeros parametros (y su orden) son el contrato que ya usan Home y Busqueda
 * (id, name, category, durationMin, imageRes, caloriesBurned); los demas son del modulo de Workouts
 * y tienen valor por defecto.
 */
data class Workout(
    val id: String,
    val name: String,
    val category: WorkoutCategory,
    val durationMin: Int,
    /** Clave de imagen resuelta en la capa de presentacion (domain no conoce Android). */
    val imageRes: String? = null,
    val caloriesBurned: Int = 0,
    val trainer: String? = null,
    val sets: Int = 0,
    val reps: Int = 0,
    val exercises: List<WorkoutExercise> = emptyList()
) {
    /** Alias usado por la UI de Home. */
    val title: String get() = name

    /** Alias usado por la UI de Home. */
    val durationMinutes: Int get() = durationMin

    /** Alias usado por el modulo de Workouts. */
    val kcal: Int get() = caloriesBurned

    /** Alias usado por el modulo de Workouts (nunca nulo). */
    val imageKey: String get() = imageRes.orEmpty()

    val totalExercises: Int get() = exercises.size
    val totalSets: Int get() = exercises.sumOf { it.sets }
}
