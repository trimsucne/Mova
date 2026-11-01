package co.edu.unal.dadm.mova.presentation.viewmodel.auth

sealed interface SignUpEvent {
    data class EmailChanged(val value: String) : SignUpEvent
    data class PasswordChanged(val value: String) : SignUpEvent
    data class ConfirmationChanged(val value: String) : SignUpEvent
    data object Submit : SignUpEvent
    data class GoogleTokenReceived(val token: String) : SignUpEvent
}
