package co.edu.unal.dadm.mova.presentation.viewmodel.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.unal.dadm.mova.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import android.net.Uri
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val repository: ProfileRepository,
    private val userId: String
) : ViewModel() {
    private val uploading = MutableStateFlow(false)
    private val avatarPath = MutableStateFlow<String?>(null)
    private val error = MutableStateFlow<String?>(null)
    val state = repository.observe(userId)
        .catch {
            error.value = "Profile unavailable. Check your connection."
            emit(null)
        }
        .onStart { emit(null) }
        .combine(uploading) { profile, isUploading ->
            Triple(profile, isUploading, avatarPath.value)
        }
        .combine(error) { (profile, isUploading, localAvatar), message ->
            ProfileUiState(
                profile = profile,
                loading = false,
                error = message,
                uploadingAvatar = isUploading,
                avatarPath = localAvatar
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfileUiState())

    fun uploadAvatar(uri: Uri, contentType: String) {
        viewModelScope.launch {
            uploading.value = true
            try {
                avatarPath.value = repository.saveAvatar(uri, contentType)
                error.value = null
            } catch (_: Exception) {
                error.value = "Avatar could not be saved locally."
            } finally {
                uploading.value = false
            }
        }

    }

    fun reportError(message: String) {
        error.value = message
    }
}
