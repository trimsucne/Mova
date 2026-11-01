package co.edu.unal.dadm.mova.presentation.screen.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.unal.dadm.mova.domain.repository.ProfileRepository
import co.edu.unal.dadm.mova.presentation.viewmodel.profile.ProfileViewModel
import co.edu.unal.dadm.mova.presentation.viewmodel.profile.ProfileViewModelFactory

@Composable
fun ProfileScreen(
    repository: ProfileRepository,
    userId: String,
    onSettings: () -> Unit,
    onSignOut: () -> Unit
) {
    val state by viewModel<ProfileViewModel>(
        factory = ProfileViewModelFactory(repository, userId)
    ).state.collectAsStateWithLifecycle()
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Profile")
        if (state.loading) CircularProgressIndicator()
        state.profile?.let { profile ->
            Text(profile.displayName.ifBlank { profile.email })
            Text("Uplift Score: ${profile.upliftScore}")
            Text("Age: ${profile.age ?: "-"}")
            Text("Weight: ${profile.weightKg?.let { "$it kg" } ?: "-"}")
        }
        state.error?.let { Text(it) }
        Button(onClick = onSettings) { Text("Account settings") }
        Button(onClick = onSignOut) { Text("Sign out") }
    }
}
