package co.edu.unal.dadm.mova.data.repository

import co.edu.unal.dadm.mova.core.common.Result
import co.edu.unal.dadm.mova.domain.model.AuthUser
import co.edu.unal.dadm.mova.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

/**
 * Local adapter for development. Replace internals with FirebaseAuth without changing domain callers.
 */
class AuthRepositoryImpl : AuthRepository {
    private val accounts = mutableMapOf<String, String>()
    private val _currentUser = MutableStateFlow<AuthUser?>(null)
    override val currentUser: StateFlow<AuthUser?> = _currentUser.asStateFlow()

    override suspend fun signIn(email: String, password: String): Result<AuthUser> {
        val normalizedEmail = email.trim().lowercase()
        return if (accounts[normalizedEmail] == password) {
            AuthUser(UUID.nameUUIDFromBytes(normalizedEmail.toByteArray()).toString(), normalizedEmail)
                .also { _currentUser.value = it }
                .let { Result.Success(it) }
        } else {
            Result.Error("Invalid email or password")
        }
    }

    override suspend fun signUp(email: String, password: String): Result<AuthUser> {
        val normalizedEmail = email.trim().lowercase()
        if (accounts.containsKey(normalizedEmail)) {
            return Result.Error("Email is already registered")
        }
        accounts[normalizedEmail] = password
        return signIn(normalizedEmail, password)
    }

    override suspend fun sendPasswordReset(email: String): Result<Unit> =
        if (accounts.containsKey(email.trim().lowercase())) {
            Result.Success(Unit)
        } else {
            Result.Error("No account found for this email")
        }

    override fun signOut() {
        _currentUser.value = null
    }
}
