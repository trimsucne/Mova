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
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val repository: ProfileRepository,
    private val userId: String
) : ViewModel() {
    private val uploading = MutableStateFlow(false)
    val state = repository.observe(userId)
        .combine(uploading) { profile, isUploading ->
            ProfileUiState(profile = profile, loading = false, uploadingAvatar = isUploading)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfileUiState())

    fun uploadAvatar(uri: Uri, contentType: String) {
        viewModelScope.launch {
            uploading.value = true
            try {
                repository.uploadAvatar(userId, uri, contentType)
            } finally {
                uploading.value = false
            }
        }
    }
}
