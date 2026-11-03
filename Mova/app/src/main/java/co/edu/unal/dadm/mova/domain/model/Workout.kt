package co.edu.unal.dadm.mova.domain.model

data class Workout(
    val id: String,
    val title: String,
    val category: WorkoutCategory,
    val trainer: String?,
    val durationMin: Int,
    val kcal: Int,
    val sets: Int,
    val reps: Int,
    /** Clave de imagen resuelta en la capa de presentacion (domain no conoce Android). */
    val imageKey: String,
    val exercises: List<WorkoutExercise>
) {
    val totalExercises: Int get() = exercises.size
    val totalSets: Int get() = exercises.sumOf { it.sets }
}
