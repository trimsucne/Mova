package co.edu.unal.dadm.mova.presentation.screen.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.unal.dadm.mova.domain.model.PremiumFeature
import co.edu.unal.dadm.mova.domain.model.PremiumTier
import co.edu.unal.dadm.mova.domain.model.UserProfile
import co.edu.unal.dadm.mova.domain.model.allows
import co.edu.unal.dadm.mova.domain.repository.ProfileRepository
import co.edu.unal.dadm.mova.presentation.viewmodel.profile.ProfileViewModel
import co.edu.unal.dadm.mova.presentation.viewmodel.profile.ProfileViewModelFactory
import co.edu.unal.dadm.mova.ui.theme.MovaField
import co.edu.unal.dadm.mova.ui.theme.MovaInk
import co.edu.unal.dadm.mova.ui.theme.MovaOrange

@Composable
fun ProfileScreen(
    repository: ProfileRepository,
    userId: String,
    onSettings: () -> Unit,
    onSignOut: () -> Unit,
    onPremiumFeature: (PremiumFeature) -> Unit = {}
) {
    val profileViewModel: ProfileViewModel = viewModel(factory = ProfileViewModelFactory(repository, userId))
    val state by profileViewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        val type = context.contentResolver.getType(uri)
        val size = context.contentResolver.openAssetFileDescriptor(uri, "r")?.use { it.length }
        if ((type == "image/jpeg" || type == "image/png") && size in 1L..5_242_880L) {
            profileViewModel.uploadAvatar(uri, type)
        } else {
            profileViewModel.reportError("Choose JPG or PNG image up to 5 MB.")
        }
    }

    Column(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ProfileHero(
            profile = state.profile,
            avatarPath = state.avatarPath,
            onSettings = onSettings,
            onAvatar = { picker.launch("image/*") }
        )
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (state.loading) CircularProgressIndicator()
            state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            UpliftCard(state.profile?.upliftScore ?: 0)
            StatsRow(state.profile)
            if (state.profile?.isPremium == true) {
                PremiumBadge()
            } else {
                TextButton(
                    onClick = { onPremiumFeature(PremiumFeature.ADAPTIVE_RECOMMENDATIONS) },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Unlock Premium") }
            }
            Button(
                onClick = onSignOut,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MovaInk,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(14.dp)
            ) { Text("Sign out") }
        }
    }
}

@Composable
private fun ProfileHero(
    profile: UserProfile?,
    avatarPath: String?,
    onSettings: () -> Unit,
    onAvatar: () -> Unit
) {
    Column(Modifier.fillMaxWidth()) {
        Box(
            Modifier.fillMaxWidth().height(132.dp)
                .background(MovaOrange),
            contentAlignment = Alignment.BottomCenter
        ) {
            Text("MOVA", color = Color.White, style = MaterialTheme.typography.headlineMedium)
            TextButton(
                onClick = onSettings,
                modifier = Modifier.align(Alignment.TopEnd)
            ) { Text("⚙", color = Color.White, style = MaterialTheme.typography.titleLarge) }
        }
        Column(
            Modifier.fillMaxWidth().padding(top = 0.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AvatarCircle(avatarPath, onAvatar)
            Text(
                profile?.displayName?.ifBlank { "Mova Member" } ?: "Mova Member",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(profile?.email.orEmpty(), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                if (profile?.isPremium == true) "Premium Member" else "Basic Member",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
private fun AvatarCircle(path: String?, onClick: () -> Unit) {
    Box(
        Modifier.size(76.dp).clip(CircleShape)
            .background(MovaField)
            .border(3.dp, Color.White, CircleShape)
            .padding(3.dp)
            .background(MovaOrange, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(if (path == null) "◎" else "☺", color = Color.White, style = MaterialTheme.typography.headlineMedium)
    }
}

@Composable
private fun UpliftCard(score: Int) {
    Column(
        Modifier.fillMaxWidth().background(MovaField, RoundedCornerShape(18.dp)).padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("◉  Uplift Score", fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            Text("Weekly", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(score.coerceIn(0, 100).toString(), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Row(
            Modifier.fillMaxWidth().height(72.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            listOf(42, 63, 38, 55, 48, 70, 50).forEachIndexed { index, value ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        Modifier.width(10.dp).height(value.dp).background(
                            if (index == 1) MovaInk else Color(0xFFB7B7B7),
                            RoundedCornerShape(6.dp)
                        )
                    )
                    Text(listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")[index], style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
private fun StatsRow(profile: UserProfile?) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        StatCard("⌁", "${profile?.age ?: "-"} yr", "Current Age", Modifier.weight(1f))
        StatCard("▾", "${profile?.weightKg?.toInt() ?: "-"} kg", "Weight", Modifier.weight(1f))
        StatCard("★", "978", "Daily Intake", Modifier.weight(1f))
    }
}

@Composable
private fun StatCard(icon: String, value: String, label: String, modifier: Modifier) {
    Column(
        modifier.background(MovaField, RoundedCornerShape(16.dp)).padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(icon, color = MovaOrange)
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun PremiumBadge() {
    Text(
        "Premium features unlocked",
        Modifier.fillMaxWidth().background(Color(0xFFFFE6D3), RoundedCornerShape(12.dp)).padding(12.dp),
        textAlign = TextAlign.Center,
        color = MovaOrange,
        fontWeight = FontWeight.Bold
    )
}
