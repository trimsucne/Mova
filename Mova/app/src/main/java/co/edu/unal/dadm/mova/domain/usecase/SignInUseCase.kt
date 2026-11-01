package co.edu.unal.dadm.mova.domain.usecase

import co.edu.unal.dadm.mova.core.common.Result
import co.edu.unal.dadm.mova.domain.model.AuthUser
import co.edu.unal.dadm.mova.domain.repository.AuthRepository

class SignInUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): Result<AuthUser> =
        repository.signIn(email.trim(), password)

    suspend fun withGoogle(idToken: String): Result<AuthUser> =
        repository.signInWithGoogle(idToken)
}
