package co.edu.unal.dadm.mova.presentation.screen.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.unal.dadm.mova.domain.repository.ProfileRepository
import co.edu.unal.dadm.mova.presentation.components.CurvedHeader
import co.edu.unal.dadm.mova.presentation.components.MovaPrimaryButton
import co.edu.unal.dadm.mova.presentation.components.MovaTextField
import co.edu.unal.dadm.mova.presentation.viewmodel.profile.ProfileViewModel
import co.edu.unal.dadm.mova.presentation.viewmodel.profile.ProfileViewModelFactory

@Composable
fun PersonalInfoScreen(
    repository: ProfileRepository,
    userId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val profileViewModel: ProfileViewModel = viewModel(factory = ProfileViewModelFactory(repository, null, userId))
    val state by profileViewModel.state.collectAsStateWithLifecycle()
    val profile = state.profile

    var name by remember(profile) { mutableStateOf(profile?.displayName?.ifBlank { "Makise Kurisu" } ?: "Makise Kurisu") }
    var email by remember(profile) { mutableStateOf(profile?.email?.ifBlank { "elementary221b@gmail.com" } ?: "elementary221b@gmail.com") }
    var locationText by remember(profile) { mutableStateOf(profile?.location?.ifBlank { "Tokyo, Japan" } ?: "Tokyo, Japan") }
    var age by remember(profile) { mutableStateOf((profile?.age ?: 17).toString()) }
    var weight by remember(profile) { mutableStateOf("${(profile?.weightKg ?: 68.0).toInt()} kg") }
    var height by remember(profile) { mutableStateOf(profile?.heightCm?.let { "$it cm" } ?: "") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CurvedHeader(title = "Personal Info", onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            MovaTextField(value = name, onValueChange = { name = it }, label = "Full Name", leadingIconText = "👤")
            MovaTextField(value = email, onValueChange = { email = it }, label = "Email Address", leadingIconText = "✉")
            MovaTextField(value = locationText, onValueChange = { locationText = it }, label = "Location", leadingIconText = "📍")
            MovaTextField(value = age, onValueChange = { age = it }, label = "Age", leadingIconText = "🎂")
            MovaTextField(value = weight, onValueChange = { weight = it }, label = "Current Weight", leadingIconText = "⚖️")
            MovaTextField(value = height, onValueChange = { height = it }, label = "Height", leadingIconText = "📏", placeholder = "170 cm")

            Spacer(modifier = Modifier.height(12.dp))

            MovaPrimaryButton(
                text = "Save Changes",
                onClick = {
                    val parsedAge = age.toIntOrNull()
                    val parsedWeight = weight.replace("[^0-9.]".toRegex(), "").toDoubleOrNull()
                    val parsedHeight = height.replace("[^0-9]".toRegex(), "").toIntOrNull()?.takeIf { it in 100..250 }
                    profileViewModel.updatePersonalInfo(name, email, locationText, parsedAge, parsedWeight, parsedHeight)
                    onBack()
                },
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            )
        }
    }
}
