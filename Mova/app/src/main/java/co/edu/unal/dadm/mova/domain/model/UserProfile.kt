package co.edu.unal.dadm.mova.domain.model

data class UserProfile(
    val userId: String,
    val email: String = "",
    val displayName: String = "",
    val avatarId: String? = null,
    val age: Int? = null,
    val weightKg: Double? = null,
    val diet: Diet? = null,
    val gender: Gender? = null,
    val fitnessLevel: FitnessLevel? = null,
    val goal: Goal? = null,
    val upliftScore: Int = 0
)
