package co.edu.unal.dadm.mova.data.repository

import co.edu.unal.dadm.mova.data.remote.FirestoreUserDataSource
import co.edu.unal.dadm.mova.data.remote.FirebaseAvatarDataSource
import android.net.Uri
import co.edu.unal.dadm.mova.domain.model.Diet
import co.edu.unal.dadm.mova.domain.model.FitnessLevel
import co.edu.unal.dadm.mova.domain.model.Gender
import co.edu.unal.dadm.mova.domain.model.Goal
import co.edu.unal.dadm.mova.domain.model.UserProfile
import co.edu.unal.dadm.mova.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow

class ProfileRepositoryImpl(
    private val dataSource: FirestoreUserDataSource = FirestoreUserDataSource()
    ,
    private val avatarDataSource: FirebaseAvatarDataSource = FirebaseAvatarDataSource()
) : ProfileRepository {
    override fun observe(userId: String): Flow<UserProfile?> = dataSource.observeUser(userId)
    override suspend fun update(userId: String, changes: Map<String, Any?>) = dataSource.updateUser(userId, changes)
    override suspend fun uploadAvatar(userId: String, uri: Uri, contentType: String): String {
        val url = avatarDataSource.upload(userId, uri, contentType)
        dataSource.updateUser(userId, mapOf("avatarUrl" to url))
        return url
    }
}
