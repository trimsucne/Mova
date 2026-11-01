package co.edu.unal.dadm.mova.presentation.viewmodel.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.unal.dadm.mova.core.common.Result
import co.edu.unal.dadm.mova.core.common.validateEmail
import co.edu.unal.dadm.mova.core.common.validatePassword
import co.edu.unal.dadm.mova.core.common.validatePasswordConfirmation
import co.edu.unal.dadm.mova.domain.usecase.SignUpUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SignUpViewModel(private val signUpUseCase: SignUpUseCase) : ViewModel() {
    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState: StateFlow<SignUpUiState> = _uiState.asStateFlow()

    fun emailChanged(value: String) { _uiState.value = _uiState.value.copy(email = value, error = null) }
    fun passwordChanged(value: String) { _uiState.value = _uiState.value.copy(password = value, error = null) }
    fun confirmationChanged(value: String) { _uiState.value = _uiState.value.copy(confirmation = value, error = null) }

    fun googleSignIn(idToken: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            _uiState.value = when (val result = signUpUseCase.withGoogle(idToken)) {
                is Result.Success -> _uiState.value.copy(isLoading = false, completed = true)
                is Result.Error -> _uiState.value.copy(isLoading = false, error = result.message)
                Result.Loading -> _uiState.value
            }
        }

    }

    fun googleSignInCancelled() {
        _uiState.value = _uiState.value.copy(error = "Google sign-in cancelled")
    }

    fun googleSignInError(message: String) {
        _uiState.value = _uiState.value.copy(isLoading = false, error = message)
    }

    fun signUp() {
        val state = _uiState.value
        val validationError = validateEmail(state.email)
            ?: validatePassword(state.password)
            ?: validatePasswordConfirmation(state.password, state.confirmation)
        if (validationError != null) {
            _uiState.value = state.copy(error = validationError)
            return
        }
        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, error = null)
            _uiState.value = when (val result = signUpUseCase(state.email, state.password)) {
                is Result.Success -> _uiState.value.copy(isLoading = false, completed = true)
                is Result.Error -> _uiState.value.copy(isLoading = false, error = result.message)
                Result.Loading -> _uiState.value
            }
        }
    }
}
