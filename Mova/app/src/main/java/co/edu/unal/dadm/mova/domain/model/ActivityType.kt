package co.edu.unal.dadm.mova.domain.model

enum class ActivityType(
    val id: String,
    val label: String,
    /** Equivalente metabolico (MET) para estimar calorias. */
    val met: Double,
    /** Si usa GPS/mapa durante el tracking. */
    val usesGps: Boolean
) {
    Running("running", "Running", 9.8, true),
    Jogging("jogging", "Jogging", 7.0, true),
    Biking("biking", "Biking", 7.5, true),
    Walking("walking", "Walking", 3.5, true),
    Hiking("hiking", "Hiking", 6.0, true),
    Swimming("swimming", "Swimming", 8.0, false),
    Weightlifting("weightlifting", "Weightlifting", 5.0, false),
    Yoga("yoga", "Yoga", 2.5, false),
    Basketball("basketball", "Basketball", 6.5, false);

    companion object {
        fun fromId(id: String?): ActivityType? = entries.firstOrNull { it.id == id }
    }
}
