package co.edu.unal.dadm.mova.presentation.viewmodel.auth

sealed interface ResetPasswordEvent {
    data class EmailChanged(val value: String) : ResetPasswordEvent
    data object Submit : ResetPasswordEvent
}
