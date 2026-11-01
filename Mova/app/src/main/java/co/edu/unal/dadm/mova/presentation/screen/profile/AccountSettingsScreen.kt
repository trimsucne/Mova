package co.edu.unal.dadm.mova.presentation.screen.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.unal.dadm.mova.domain.repository.UserPreferencesRepository
import co.edu.unal.dadm.mova.domain.model.UnitSystem
import co.edu.unal.dadm.mova.presentation.viewmodel.profile.AccountSettingsViewModel
import co.edu.unal.dadm.mova.presentation.viewmodel.profile.AccountSettingsViewModelFactory

@Composable
fun AccountSettingsScreen(repository: UserPreferencesRepository, onBack: () -> Unit) {
    val settingsViewModel: AccountSettingsViewModel = viewModel(
        factory = AccountSettingsViewModelFactory(repository)
    )
    val state by settingsViewModel.state.collectAsStateWithLifecycle()
    Column(Modifier.fillMaxSize().padding(24.dp)) {
        TextButton(onClick = onBack) { Text("Back") }
        Text("Account settings")
        Text("Dark mode")
        Switch(
            checked = state.preferences.darkMode,
            onCheckedChange = settingsViewModel::setDarkMode
        )
        Text("Notifications")
        Switch(
            checked = state.preferences.notificationsEnabled,
            onCheckedChange = settingsViewModel::setNotifications
        )
        Text("Units")
        TextButton(onClick = {
            settingsViewModel.setUnits(
                if (state.preferences.unitSystem == UnitSystem.METRIC) UnitSystem.IMPERIAL
                else UnitSystem.METRIC
            )
        }) {
            Text(state.preferences.unitSystem.name)
        }
    }
}
