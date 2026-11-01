package co.edu.unal.dadm.mova.presentation.viewmodel.profile

import co.edu.unal.dadm.mova.domain.model.UserProfile

data class ProfileUiState(
    val profile: UserProfile? = null,
    val loading: Boolean = true,
    val error: String? = null,
    val uploadingAvatar: Boolean = false
)
