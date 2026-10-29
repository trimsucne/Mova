package co.edu.unal.dadm.mova.presentation.viewmodel.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import co.edu.unal.dadm.mova.domain.repository.AuthRepository

class AuthViewModelFactory(
    private val repository: AuthRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(SignInViewModel::class.java) ->
                SignInViewModel(repository) as T
            modelClass.isAssignableFrom(SignUpViewModel::class.java) ->
                SignUpViewModel(repository) as T
            modelClass.isAssignableFrom(ResetPasswordViewModel::class.java) ->
                ResetPasswordViewModel(repository) as T
            else -> error("Unsupported ViewModel: ${modelClass.name}")
        }
    }
}
