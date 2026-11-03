package co.edu.unal.dadm.mova.domain.model

data class WorkoutExercise(
    val id: String,
    val name: String,
    val sets: Int,
    val reps: Int,
    val restSeconds: Int = 30
)
