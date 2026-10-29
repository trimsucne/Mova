package co.edu.unal.dadm.mova.presentation.viewmodel.auth

data class SignUpUiState(
    val email: String = "",
    val password: String = "",
    val confirmation: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val completed: Boolean = false
)
