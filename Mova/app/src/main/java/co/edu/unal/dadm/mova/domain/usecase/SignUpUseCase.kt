package co.edu.unal.dadm.mova.domain.usecase

import co.edu.unal.dadm.mova.core.common.Result
import co.edu.unal.dadm.mova.domain.model.AuthUser
import co.edu.unal.dadm.mova.domain.repository.AuthRepository

class SignUpUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): Result<AuthUser> =
        repository.signUp(email.trim(), password)
}
