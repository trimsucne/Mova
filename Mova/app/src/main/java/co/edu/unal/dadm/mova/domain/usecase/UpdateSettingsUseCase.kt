package co.edu.unal.dadm.mova.domain.usecase

import co.edu.unal.dadm.mova.domain.model.UnitSystem
import co.edu.unal.dadm.mova.domain.repository.UserPreferencesRepository

class UpdateSettingsUseCase(private val repository: UserPreferencesRepository) {
    suspend fun darkMode(enabled: Boolean) = repository.setDarkMode(enabled)
    suspend fun units(unitSystem: UnitSystem) = repository.setUnitSystem(unitSystem)
    suspend fun notifications(enabled: Boolean) = repository.setNotificationsEnabled(enabled)
}
