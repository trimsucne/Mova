package co.edu.unal.dadm.mova.presentation.screen.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import co.edu.unal.dadm.mova.ui.theme.MovaOrange

@Composable
fun AvatarStep(selected: String?, onSelected: (String) -> Unit, onContinue: () -> Unit) {
    AssessmentStep(6, "Select your Avatar", "We have 23 custom premade avatars, or\nyou can upload profile locally.", selected != null, onContinue) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            listOf("avatar_1", "avatar_2", "avatar_3").forEach { avatar ->
                OptionButton(if (selected == avatar) "●" else "○", selected == avatar) { onSelected(avatar) }
            }
        }
        Text("⇧", Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center, color = MovaOrange, style = androidx.compose.material3.MaterialTheme.typography.displaySmall)
        Text("or Upload from Local File", Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Text("Max 5mb, Format: jpg,png", Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}
