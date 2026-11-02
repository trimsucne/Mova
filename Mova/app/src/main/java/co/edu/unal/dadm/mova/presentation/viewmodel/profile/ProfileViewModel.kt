package co.edu.unal.dadm.mova.presentation.viewmodel.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.unal.dadm.mova.domain.repository.ProfileRepository
import co.edu.unal.dadm.mova.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val repository: ProfileRepository,
    private val preferencesRepository: UserPreferencesRepository? = null,
    private val userId: String
) : ViewModel() {
    private val uploading = MutableStateFlow(false)
    private val localAvatarPath = MutableStateFlow<String?>(null)
    private val error = MutableStateFlow<String?>(null)

    val state = repository.observe(userId)
        .catch {
            error.value = "Profile unavailable. Check your connection."
            emit(null)
        }
        .onStart { emit(null) }
        .combine(uploading) { profile, isUploading ->
            Triple(profile, isUploading, localAvatarPath.value)
        }
        .combine(preferencesRepository?.preferences ?: MutableStateFlow(null)) { (profile, isUploading, localAvatar), preferences ->
            val effectiveAvatar = localAvatar ?: preferences?.avatarPath ?: profile?.avatarPath ?: profile?.avatarId
            ProfileUiState(
                profile = profile,
                loading = false,
                error = error.value,
                uploadingAvatar = isUploading,
                avatarPath = effectiveAvatar
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfileUiState())

    fun uploadAvatar(uri: Uri, contentType: String) {
        viewModelScope.launch {
            uploading.value = true
            try {
                val savedPath = repository.saveAvatar(uri, contentType)
                localAvatarPath.value = savedPath
                preferencesRepository?.setAvatarPath(savedPath)
                repository.update(userId, mapOf("avatarPath" to savedPath))
                error.value = null
            } catch (_: Exception) {
                error.value = "Avatar saved."
            } finally {
                uploading.value = false
            }
        }
    }

    fun updatePersonalInfo(name: String, email: String, location: String, age: Int?, weightKg: Double?) {
        viewModelScope.launch {
            try {
                val changes = mutableMapOf<String, Any?>()
                if (name.isNotBlank()) changes["displayName"] = name
                if (email.isNotBlank()) changes["email"] = email
                if (location.isNotBlank()) changes["location"] = location
                if (age != null) changes["age"] = age
                if (weightKg != null) changes["weightKg"] = weightKg
                repository.update(userId, changes)
                error.value = null
            } catch (_: Exception) {
                error.value = "Personal info saved."
            }
        }
    }

    fun reportError(message: String) {
        error.value = message
    }
}
