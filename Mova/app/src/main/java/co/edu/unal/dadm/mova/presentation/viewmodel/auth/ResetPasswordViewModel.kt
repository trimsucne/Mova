package co.edu.unal.dadm.mova.presentation.viewmodel.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.unal.dadm.mova.core.common.Result
import co.edu.unal.dadm.mova.core.common.validateEmail
import co.edu.unal.dadm.mova.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ResetPasswordViewModel(private val repository: AuthRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(ResetPasswordUiState())
    val uiState: StateFlow<ResetPasswordUiState> = _uiState.asStateFlow()

    fun emailChanged(value: String) { _uiState.value = _uiState.value.copy(email = value, error = null) }

    fun sendReset() {
        val state = _uiState.value
        val validationError = validateEmail(state.email)
        if (validationError != null) {
            _uiState.value = state.copy(error = validationError)
            return
        }
        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, error = null)
            _uiState.value = when (val result = repository.sendPasswordReset(state.email)) {
                is Result.Success -> _uiState.value.copy(isLoading = false, sent = true)
                is Result.Error -> _uiState.value.copy(isLoading = false, error = result.message)
                Result.Loading -> _uiState.value
            }
        }
    }
}
