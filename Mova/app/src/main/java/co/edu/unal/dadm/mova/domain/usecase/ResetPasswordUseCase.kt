package co.edu.unal.dadm.mova.domain.usecase

import co.edu.unal.dadm.mova.core.common.Result
import co.edu.unal.dadm.mova.domain.repository.AuthRepository

class ResetPasswordUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String): Result<Unit> =
        repository.sendPasswordReset(email.trim())
}
