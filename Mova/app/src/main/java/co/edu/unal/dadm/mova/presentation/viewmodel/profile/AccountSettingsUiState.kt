package co.edu.unal.dadm.mova.presentation.viewmodel.profile

import co.edu.unal.dadm.mova.domain.repository.UserPreferences

data class AccountSettingsUiState(
    val preferences: UserPreferences = UserPreferences(),
    val error: String? = null
)
