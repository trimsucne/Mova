package co.edu.unal.dadm.mova.data.entity

data class UserEntity(
    val id: String,
    val email: String,
    val displayName: String = "",
    val photoUrl: String? = null
)
