package co.edu.unal.dadm.mova.presentation.viewmodel.auth

data class ResetPasswordUiState(
    val email: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val sent: Boolean = false
)
