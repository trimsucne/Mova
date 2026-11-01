package co.edu.unal.dadm.mova.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import co.edu.unal.dadm.mova.domain.model.UnitSystem
import co.edu.unal.dadm.mova.domain.repository.UserPreferences as DomainPreferences
import co.edu.unal.dadm.mova.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.userPreferencesDataStore by preferencesDataStore("user_preferences")

class UserPreferencesImpl(context: Context) : UserPreferencesRepository {
    private val dataStore = context.userPreferencesDataStore

    override val preferences: Flow<DomainPreferences> = dataStore.data.map { values ->
        DomainPreferences(
            darkMode = values[DARK_MODE] ?: false,
            unitSystem = values[UNIT_SYSTEM]?.let { runCatching { UnitSystem.valueOf(it) }.getOrNull() }
                ?: UnitSystem.METRIC,
            notificationsEnabled = values[NOTIFICATIONS] ?: true
        )
    }

    override suspend fun setDarkMode(enabled: Boolean) { dataStore.edit { it[DARK_MODE] = enabled } }
    override suspend fun setUnitSystem(unitSystem: UnitSystem) { dataStore.edit { it[UNIT_SYSTEM] = unitSystem.name } }
    override suspend fun setNotificationsEnabled(enabled: Boolean) { dataStore.edit { it[NOTIFICATIONS] = enabled } }

    private companion object {
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val UNIT_SYSTEM = stringPreferencesKey("unit_system")
        val NOTIFICATIONS = booleanPreferencesKey("notifications")
    }
}
