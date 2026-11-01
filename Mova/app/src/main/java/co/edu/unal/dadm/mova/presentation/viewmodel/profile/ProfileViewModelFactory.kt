package co.edu.unal.dadm.mova.presentation.viewmodel.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import co.edu.unal.dadm.mova.domain.repository.ProfileRepository

class ProfileViewModelFactory(
    private val repository: ProfileRepository,
    private val userId: String
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        ProfileViewModel(repository, userId) as T
}
