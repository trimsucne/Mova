package co.edu.unal.dadm.mova.data.repository

import co.edu.unal.dadm.mova.core.common.Result
import co.edu.unal.dadm.mova.data.remote.FirebaseAuthDataSource
import co.edu.unal.dadm.mova.data.remote.FirestoreUserDataSource
import co.edu.unal.dadm.mova.domain.model.AuthUser
import co.edu.unal.dadm.mova.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class AuthRepositoryImpl(
    private val authDataSource: FirebaseAuthDataSource = FirebaseAuthDataSource(),
    private val userDataSource: FirestoreUserDataSource = FirestoreUserDataSource()
) : AuthRepository {
    private val scope = CoroutineScope(Dispatchers.IO)

    override val currentUser: StateFlow<AuthUser?> = authDataSource.currentUser
        .map { it?.toDomain() }
        .stateIn(scope, SharingStarted.Eagerly, null)

    override suspend fun signIn(email: String, password: String): Result<AuthUser> =
        runFirebaseCall {
            authDataSource.signIn(email, password).user.requireDomain()
        }

    override suspend fun signUp(email: String, password: String): Result<AuthUser> =
        runFirebaseCall {
            authDataSource.signUp(email, password).user.requireDomain()
        }

    override suspend fun signInWithGoogle(idToken: String): Result<AuthUser> =
        runFirebaseCall {
            authDataSource.signInWithGoogle(idToken).user.requireDomain()
        }

    override suspend fun sendPasswordReset(email: String): Result<Unit> =
        runFirebaseCall {
            authDataSource.sendPasswordReset(email)
        }

    override fun signOut() {
        authDataSource.signOut()
    }

    private suspend fun <T> runFirebaseCall(block: suspend () -> T): Result<T> =
        try {
            val value = block()
            val firebaseUser = authDataSource.signedInUser
            if (firebaseUser != null) userDataSource.ensureUserDocument(firebaseUser)
            Result.Success(value)
        } catch (exception: Exception) {
            Result.Error(exception.message ?: "Authentication request failed")
        }

    private fun FirebaseUser.toDomain() = AuthUser(uid, email.orEmpty())

    private fun FirebaseUser?.requireDomain(): AuthUser =
        requireNotNull(this) { "Firebase returned no authenticated user" }.toDomain()
}
