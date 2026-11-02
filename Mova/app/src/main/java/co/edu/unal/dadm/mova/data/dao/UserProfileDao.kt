package co.edu.unal.dadm.mova.data.dao

import co.edu.unal.dadm.mova.data.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class UserProfileDao {
    fun getProfile(userId: String): Flow<UserProfileEntity?> = flowOf(null)
    suspend fun insertProfile(profile: UserProfileEntity) {}
    suspend fun updateProfile(profile: UserProfileEntity) {}
    suspend fun updateAvatar(userId: String, avatarPath: String) {}
}
