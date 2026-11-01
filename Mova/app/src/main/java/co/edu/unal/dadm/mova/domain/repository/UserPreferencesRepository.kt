package co.edu.unal.dadm.mova.domain.repository

import co.edu.unal.dadm.mova.domain.model.UnitSystem
import kotlinx.coroutines.flow.Flow

data class UserPreferences(
    val darkMode: Boolean = false,
    val unitSystem: UnitSystem = UnitSystem.METRIC,
    val notificationsEnabled: Boolean = true
)

interface UserPreferencesRepository {
    val preferences: Flow<UserPreferences>
    suspend fun setDarkMode(enabled: Boolean)
    suspend fun setUnitSystem(unitSystem: UnitSystem)
    suspend fun setNotificationsEnabled(enabled: Boolean)
}
