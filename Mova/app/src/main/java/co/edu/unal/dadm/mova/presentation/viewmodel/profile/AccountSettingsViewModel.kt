package co.edu.unal.dadm.mova.presentation.viewmodel.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.unal.dadm.mova.domain.model.UnitSystem
import co.edu.unal.dadm.mova.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AccountSettingsViewModel(private val repository: UserPreferencesRepository) : ViewModel() {
    val state = repository.preferences.map { AccountSettingsUiState(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AccountSettingsUiState())

    fun setDarkMode(enabled: Boolean) = viewModelScope.launch { repository.setDarkMode(enabled) }
    fun setNotifications(enabled: Boolean) = viewModelScope.launch { repository.setNotificationsEnabled(enabled) }
    fun setUnits(units: UnitSystem) = viewModelScope.launch { repository.setUnitSystem(units) }
}
