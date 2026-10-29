package co.edu.unal.dadm.mova.presentation.viewmodel.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.unal.dadm.mova.core.common.Result
import co.edu.unal.dadm.mova.core.common.validateEmail
import co.edu.unal.dadm.mova.core.common.validatePassword
import co.edu.unal.dadm.mova.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SignInViewModel(private val repository: AuthRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(SignInUiState())
    val uiState: StateFlow<SignInUiState> = _uiState.asStateFlow()

    fun emailChanged(value: String) { _uiState.value = _uiState.value.copy(email = value, error = null) }
    fun passwordChanged(value: String) { _uiState.value = _uiState.value.copy(password = value, error = null) }

    fun signIn() {
        val state = _uiState.value
        val validationError = validateEmail(state.email) ?: validatePassword(state.password)
        if (validationError != null) {
            _uiState.value = state.copy(error = validationError)
            return
        }
        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, error = null)
            _uiState.value = when (val result = repository.signIn(state.email, state.password)) {
                is Result.Success -> _uiState.value.copy(isLoading = false, completed = true)
                is Result.Error -> _uiState.value.copy(isLoading = false, error = result.message)
                Result.Loading -> _uiState.value
            }
        }
    }
}
