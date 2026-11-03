package co.edu.unal.dadm.mova.domain.model

enum class WorkoutCategory(val id: String, val label: String, val description: String) {
    Strength("strength", "Strength", "Build your muscles bigger and stronger with this exercise. Train everyday to get bulk"),
    Cardio("cardio", "Cardio", "Raise your heart rate and burn calories with high-energy sessions"),
    Yoga("yoga", "Yoga", "Improve flexibility, balance and calm with guided flows"),
    Hiit("hiit", "HIIT", "Short, intense intervals to maximise results in minimum time");

    companion object {
        fun fromId(id: String?): WorkoutCategory = entries.firstOrNull { it.id == id } ?: Strength
    }
}
