package co.edu.unal.dadm.mova.presentation.screen.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import co.edu.unal.dadm.mova.presentation.screen.auth.components.MovaButton

@Composable
fun ResetSentScreen(onBackToSignIn: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("✓", style = MaterialTheme.typography.displaySmall)
        Text("Password Sent!", style = MaterialTheme.typography.headlineSmall)
        Text("We've sent the password reset email. Resend if needed.")
        MovaButton("Re-Send Password   🔒", onClick = onBackToSignIn)
        Text("×", modifier = Modifier.padding(top = 20.dp))
    }
}
