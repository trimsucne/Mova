package co.edu.unal.dadm.mova.presentation.screen.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.unal.dadm.mova.R
import co.edu.unal.dadm.mova.data.preferences.UserPreferencesImpl
import co.edu.unal.dadm.mova.domain.model.PremiumFeature
import co.edu.unal.dadm.mova.domain.model.UserProfile
import co.edu.unal.dadm.mova.domain.repository.ProfileRepository
import co.edu.unal.dadm.mova.presentation.charts.BarChartWeekly
import co.edu.unal.dadm.mova.presentation.components.ImagePlaceholder
import co.edu.unal.dadm.mova.presentation.theme.MovaField
import co.edu.unal.dadm.mova.presentation.theme.MovaInk
import co.edu.unal.dadm.mova.presentation.theme.MovaMuted
import co.edu.unal.dadm.mova.presentation.theme.MovaOrange
import co.edu.unal.dadm.mova.presentation.viewmodel.profile.ProfileViewModel
import co.edu.unal.dadm.mova.presentation.viewmodel.profile.ProfileViewModelFactory

@Composable
fun ProfileScreen(
    repository: ProfileRepository,
    userId: String,
    onSettings: () -> Unit,
    onAvatarPicker: () -> Unit,
    onSignOut: () -> Unit,
    onPremiumFeature: (PremiumFeature) -> Unit = {}
) {
    val context = LocalContext.current
    val preferencesRepository = remember(context) { UserPreferencesImpl(context) }
    val profileViewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModelFactory(repository, preferencesRepository, userId)
    )
    val state by profileViewModel.state.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ProfileHero(
                profile = state.profile,
                avatarPath = state.avatarPath,
                onSettings = onSettings,
                onAvatar = onAvatarPicker
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (state.loading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                }

                state.error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                }

                // Uplift Score Card
                UpliftScoreCard(score = state.profile?.upliftScore ?: 95)

                // 3 Stat Cards Row
                StatsRow(profile = state.profile)

                if (state.profile?.isPremium != true) {
                    TextButton(
                        onClick = { onPremiumFeature(PremiumFeature.ADAPTIVE_RECOMMENDATIONS) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Unlock Premium", color = MovaOrange, fontWeight = FontWeight.Bold)
                    }
                }

                Button(
                    onClick = onSignOut,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Sign Out", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Bottom Orange Action Indicator
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp)
                .size(56.dp)
                .clip(CircleShape)
                .background(MovaOrange)
                .border(4.dp, Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(MovaOrange)
            )
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
    val effectiveAvatar = avatarPath ?: profile?.avatarPath ?: profile?.avatarId ?: "avatar_2"
    val avatarDrawableRes = when (effectiveAvatar) {
        "avatar_1" -> R.drawable.img_avatar_1
        "avatar_2" -> R.drawable.img_avatar_2
        "avatar_3" -> R.drawable.img_avatar_3
        else -> null
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(230.dp)
    ) {
        // Hero Gym Banner Image
        ImagePlaceholder(
            label = "Gym Equipment Profile Hero Banner",
            drawableResId = R.drawable.img_welcome_bg,
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
            shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp),
            backgroundColor = MovaOrange
        )

        // Settings gear button on top right
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.25f))
                .clickable(onClick = onSettings),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "⚙",
                fontSize = 20.sp,
                color = Color.White
            )
        }

        // Overlapping Profile Picture + Text
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Picture Circle Cutout
            Box(
                modifier = Modifier
                    .size(92.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .padding(3.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onAvatar),
                contentAlignment = Alignment.Center
            ) {
                ImagePlaceholder(
                    label = "Profile Picture",
                    drawableResId = avatarDrawableRes,
                    imagePath = if (avatarDrawableRes == null) effectiveAvatar else null,
                    modifier = Modifier.fillMaxSize(),
                    shape = CircleShape,
                    backgroundColor = MovaField
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = profile?.displayName?.ifBlank { "Makise Kurisu" } ?: "Makise Kurisu",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(2.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = profile?.location?.ifBlank { "Tokyo, Japan" } ?: "Tokyo, Japan",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MovaMuted
                )

                Text(
                    text = "  •  ",
                    fontSize = 13.sp,
                    color = MovaMuted
                )

                Text(
                    text = if (profile?.isPremium == true) "Premium Member" else "Basic Member",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MovaMuted
                )
            }
        }
    }
}

@Composable
private fun UpliftScoreCard(score: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🍊", fontSize = 18.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Uplift Score",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Weekly",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MovaMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Weekly Bar Chart
        BarChartWeekly(
            score = score,
            selectedDayIndex = 1
        )
    }
}

@Composable
private fun StatsRow(profile: UserProfile?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            icon = "🍊",
            value = "${profile?.age ?: 17}yr",
            label = "Current Age",
            modifier = Modifier.weight(1f)
        )

        StatCard(
            icon = "🍸",
            value = "${profile?.weightKg?.toInt() ?: 68}kg",
            label = "Weight",
            modifier = Modifier.weight(1f)
        )

        StatCard(
            icon = "⭐",
            value = "978kalc",
            label = "Daily Intake",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatCard(
    icon: String,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(icon, fontSize = 20.sp)

        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = label,
            fontSize = 11.sp,
            color = MovaMuted,
            fontWeight = FontWeight.Medium
        )
    }
}
