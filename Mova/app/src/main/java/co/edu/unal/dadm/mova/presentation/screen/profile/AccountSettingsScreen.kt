package co.edu.unal.dadm.mova.presentation.screen.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.unal.dadm.mova.domain.model.UnitSystem
import co.edu.unal.dadm.mova.domain.repository.UserPreferencesRepository
import co.edu.unal.dadm.mova.presentation.viewmodel.profile.AccountSettingsViewModel
import co.edu.unal.dadm.mova.presentation.viewmodel.profile.AccountSettingsViewModelFactory
import co.edu.unal.dadm.mova.ui.theme.MovaField
import co.edu.unal.dadm.mova.ui.theme.MovaInk
import co.edu.unal.dadm.mova.ui.theme.MovaOrange

@Composable
fun AccountSettingsScreen(repository: UserPreferencesRepository, onBack: () -> Unit) {
    val settingsViewModel: AccountSettingsViewModel = viewModel(
        factory = AccountSettingsViewModelFactory(repository)
    )
    val state by settingsViewModel.state.collectAsStateWithLifecycle()
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).background(MaterialTheme.colorScheme.background).padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SettingsHeader(onBack)
        Text("General", style = MaterialTheme.typography.titleMedium)
        SettingsRow("♧", "Notification")
        SettingsRow("♙", "Personal Information")
        SettingsRow("♧", "Coach Contact")
        SettingsRow("⚑", "Dark Mode") {
            Switch(
                checked = state.preferences.darkMode,
                onCheckedChange = settingsViewModel::setDarkMode,
                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = MovaOrange)
            )
        }
        SettingsRow("⊙", "Linked Devices")
        Text("Security & Privacy", style = MaterialTheme.typography.titleMedium)
        SettingsRow("♙", "Main Security")
        SettingsRow("⚙", "Units: ${state.preferences.unitSystem.name.lowercase().replaceFirstChar { it.uppercase() }}") {
            TextButton(
                onClick = {
                    settingsViewModel.setUnits(
                        if (state.preferences.unitSystem == UnitSystem.METRIC) UnitSystem.IMPERIAL else UnitSystem.METRIC
                    )
                }
            ) { Text("Change", color = MovaOrange) }
        }
        SettingsRow("♧", "Notifications enabled") {
            Switch(
                checked = state.preferences.notificationsEnabled,
                onCheckedChange = settingsViewModel::setNotifications,
                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = MovaOrange)
            )
        }
    }
}

@Composable
private fun SettingsHeader(onBack: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().background(MovaInk, RoundedCornerShape(bottomStart = 34.dp, bottomEnd = 34.dp))
            .padding(horizontal = 4.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TextButton(onClick = onBack) { Text("↩", color = Color.White, style = MaterialTheme.typography.titleLarge) }
        Text("Account Settings", color = Color.White, style = MaterialTheme.typography.headlineSmall)
    }
}

@Composable
private fun SettingsRow(
    icon: String,
    title: String,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        Modifier.fillMaxWidth().background(MovaField, RoundedCornerShape(14.dp)).padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            icon,
            Modifier.size(34.dp).background(Color.White, RoundedCornerShape(10.dp)).padding(7.dp),
            color = MovaInk
        )
        Text(title, Modifier.padding(start = 8.dp), style = MaterialTheme.typography.labelLarge)
        Spacer(Modifier.weight(1f))
        trailing?.invoke() ?: Text("›", style = MaterialTheme.typography.titleLarge)
    }
}
