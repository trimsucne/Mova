package co.edu.unal.dadm.mova.presentation.screen.auth.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun SocialLoginRow(
    enabled: Boolean,
    onGoogleClick: () -> Unit
) {
    OutlinedButton(
        onClick = onGoogleClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Continue with Google")
    }
}
