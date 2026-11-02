package co.edu.unal.dadm.mova.domain.model

data class UserProfile(
    val userId: String,
    val email: String = "",
    val displayName: String = "",
    val location: String = "Tokyo, Japan",
    val avatarId: String? = null,
    val avatarUrl: String? = null,
    val avatarPath: String? = null,
    val age: Int? = null,
    val weightKg: Double? = null,
    val heightCm: Int? = null,
    val diet: Diet? = null,
    val gender: Gender? = null,
    val fitnessLevel: FitnessLevel? = null,
    val goal: Goal? = null,
    val upliftScore: Int = 95,
    val isPremium: Boolean = false
)
