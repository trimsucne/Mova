package co.edu.unal.dadm.mova.testutil

import android.net.Uri
import co.edu.unal.dadm.mova.domain.model.UserProfile
import co.edu.unal.dadm.mova.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeProfileRepository(profile: UserProfile? = null) : ProfileRepository {
    val profile = MutableStateFlow(profile)
    override fun observe(userId: String): Flow<UserProfile?> = profile
    override suspend fun update(userId: String, changes: Map<String, Any?>) = Unit
    override suspend fun saveAvatar(uri: Uri, contentType: String): String = ""
}
