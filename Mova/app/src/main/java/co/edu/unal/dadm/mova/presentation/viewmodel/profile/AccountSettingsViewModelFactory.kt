package co.edu.unal.dadm.mova.presentation.viewmodel.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import co.edu.unal.dadm.mova.domain.repository.UserPreferencesRepository

class AccountSettingsViewModelFactory(private val repository: UserPreferencesRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        AccountSettingsViewModel(repository) as T
}
