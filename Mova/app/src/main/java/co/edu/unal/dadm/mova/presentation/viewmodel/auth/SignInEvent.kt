package co.edu.unal.dadm.mova.presentation.viewmodel.auth

sealed interface SignInEvent {
    data class EmailChanged(val value: String) : SignInEvent
    data class PasswordChanged(val value: String) : SignInEvent
    data object Submit : SignInEvent
    data class GoogleTokenReceived(val token: String) : SignInEvent
}
