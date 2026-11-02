package co.edu.unal.dadm.mova.data.entity

data class UserProfileEntity(
    val userId: String,
    val email: String = "",
    val displayName: String = "",
    val avatarPath: String? = null,
    val age: Int? = null,
    val weightKg: Double? = null,
    val location: String = "Tokyo, Japan",
    val isPremium: Boolean = false,
    val upliftScore: Int = 95
)
