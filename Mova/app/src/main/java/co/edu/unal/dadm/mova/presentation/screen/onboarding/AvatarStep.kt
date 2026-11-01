package co.edu.unal.dadm.mova.presentation.screen.onboarding

import androidx.compose.runtime.Composable

@Composable
fun AvatarStep(selected: String?, onSelected: (String) -> Unit, onContinue: () -> Unit) {
    AssessmentStep(
        7,
        "Select your Avatar",
        "Choose a premade avatar. Upload support comes later.",
        selected != null,
        onContinue,
        progressLabel = "Profile setup"
    ) {
        listOf("avatar_1", "avatar_2", "avatar_3").forEach { avatar ->
            OptionButton(avatar.replace('_', ' ').replaceFirstChar { it.uppercase() }, selected == avatar) {
                onSelected(avatar)
            }
        }
    }
}
