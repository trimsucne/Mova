package co.edu.unal.dadm.mova.domain.model

/**
 * Entrenamiento. Campos base según la propuesta del spec de Búsqueda (§12):
 * id, name, category, durationMin, imageRes. caloriesBurned y los alias title/durationMinutes
 * los usa la Home (PR #5).
 *
 * Temporary fake until the Workout module is implemented.
 */
data class Workout(
    val id: String,
    val name: String,
    val category: WorkoutCategory,
    val durationMin: Int,
    val imageRes: String? = null,
    val caloriesBurned: Int = 0
) {
    /** Alias usado por la UI de Home (PR #5). */
    val title: String get() = name

    /** Alias usado por la UI de Home (PR #5). */
    val durationMinutes: Int get() = durationMin
}
