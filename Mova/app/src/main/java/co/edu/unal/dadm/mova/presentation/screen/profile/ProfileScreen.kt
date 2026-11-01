package co.edu.unal.dadm.mova.presentation.screen.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import android.content.Context
import android.net.Uri
import co.edu.unal.dadm.mova.domain.model.PremiumFeature
import co.edu.unal.dadm.mova.domain.model.PremiumTier
import co.edu.unal.dadm.mova.domain.model.allows
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
    onSignOut: () -> Unit,
    onPremiumFeature: (PremiumFeature) -> Unit = {}
) {
    val profileViewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModelFactory(repository, userId)
    )
    val state by profileViewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            val type = context.contentResolver.getType(uri)
            val size = context.contentResolver.openAssetFileDescriptor(uri, "r")?.use { it.length }
            if ((type == "image/jpeg" || type == "image/png") &&
                size != null && size in 1L..5_242_880L
            ) {
                profileViewModel.uploadAvatar(uri, type)
            }
        }
    }
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Profile")
        if (state.loading) CircularProgressIndicator()
        state.profile?.let { profile ->
            Text(profile.displayName.ifBlank { profile.email })
            Text(if (profile.isPremium) "Premium" else "Basic")
            Text(if (profile.avatarUrl == null) "No avatar uploaded" else "Avatar uploaded")
            Text("Uplift Score: ${profile.upliftScore}")
            Text("Age: ${profile.age ?: "-"}")
            Text("Weight: ${profile.weightKg?.let { "$it kg" } ?: "-"}")
        }
        Button(onClick = { picker.launch("image/*") }, enabled = !state.uploadingAvatar) {
            Text(if (state.uploadingAvatar) "Uploading..." else "Upload avatar")
        }
        if (profileAllowsPremium(state.profile)) {
            Text("Premium features unlocked")
        } else {
            TextButton(onClick = { onPremiumFeature(PremiumFeature.ADAPTIVE_RECOMMENDATIONS) }) {
                Text("Unlock Premium")
            }
        }
        state.error?.let { Text(it) }
        Button(onClick = onSettings) { Text("Account settings") }
        Button(onClick = onSignOut) { Text("Sign out") }
    }
}

private fun profileAllowsPremium(profile: co.edu.unal.dadm.mova.domain.model.UserProfile?): Boolean =
    profile?.isPremium == true && PremiumTier.PREMIUM.allows(PremiumFeature.ADAPTIVE_RECOMMENDATIONS)
