package co.edu.unal.dadm.mova.presentation.viewmodel.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import co.edu.unal.dadm.mova.domain.repository.AuthRepository
import co.edu.unal.dadm.mova.domain.usecase.ResetPasswordUseCase
import co.edu.unal.dadm.mova.domain.usecase.SignInUseCase
import co.edu.unal.dadm.mova.domain.usecase.SignUpUseCase

class AuthViewModelFactory(
    private val repository: AuthRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(SignInViewModel::class.java) ->
                SignInViewModel(SignInUseCase(repository)) as T
            modelClass.isAssignableFrom(SignUpViewModel::class.java) ->
                SignUpViewModel(SignUpUseCase(repository)) as T
            modelClass.isAssignableFrom(ResetPasswordViewModel::class.java) ->
                ResetPasswordViewModel(ResetPasswordUseCase(repository)) as T
            else -> error("Unsupported ViewModel: ${modelClass.name}")
        }
    }
}
