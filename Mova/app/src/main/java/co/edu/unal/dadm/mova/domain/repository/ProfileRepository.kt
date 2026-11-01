package co.edu.unal.dadm.mova.domain.repository

import co.edu.unal.dadm.mova.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    fun observe(userId: String): Flow<UserProfile?>
    suspend fun update(userId: String, changes: Map<String, Any?>)
}
