package co.edu.unal.dadm.mova.data.repository

import co.edu.unal.dadm.mova.core.common.Result
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthRepositoryImplTest {
    @Test
    fun `local account can sign up and sign in`() = runBlocking {
        val repository = AuthRepositoryImpl()

        assertTrue(repository.signUp("user@mova.test", "12345678") is Result.Success)
        repository.signOut()
        assertTrue(repository.signIn("user@mova.test", "12345678") is Result.Success)
    }

    @Test
    fun `unknown account cannot reset password`() = runBlocking {
        val repository = AuthRepositoryImpl()

        assertTrue(repository.sendPasswordReset("missing@mova.test") is Result.Error)
    }
}
