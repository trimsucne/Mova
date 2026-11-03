package co.edu.unal.dadm.mova.domain.model

/**
 * Categoria de entrenamiento. Los nombres de constante (STRENGTH, CARDIO, HIIT, YOGA, MOBILITY)
 * son los que ya usan Home y Busqueda; id/label/description los usa el modulo de Workouts.
 */
enum class WorkoutCategory(val id: String, val label: String, val description: String) {
    STRENGTH("strength", "Strength", "Build your muscles bigger and stronger with this exercise. Train everyday to get bulk"),
    CARDIO("cardio", "Cardio", "Raise your heart rate and burn calories with high-energy sessions"),
    YOGA("yoga", "Yoga", "Improve flexibility, balance and calm with guided flows"),
    HIIT("hiit", "HIIT", "Short, intense intervals to maximise results in minimum time"),
    MOBILITY("mobility", "Mobility", "Loosen up joints and improve range of motion with gentle movement");

    companion object {
        fun fromId(id: String?): WorkoutCategory = entries.firstOrNull { it.id == id } ?: STRENGTH
    }
}
