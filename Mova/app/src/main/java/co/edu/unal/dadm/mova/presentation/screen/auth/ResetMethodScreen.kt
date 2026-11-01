package co.edu.unal.dadm.mova.presentation.screen.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import co.edu.unal.dadm.mova.presentation.screen.auth.components.AuthHeader
import co.edu.unal.dadm.mova.presentation.screen.auth.components.MovaButton

@Composable
fun ResetMethodScreen(onEmailSelected: () -> Unit, onGoogleSelected: () -> Unit, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(10.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        AuthHeader("Reset Password", "Select what method you'd like to use.", onBack)
        ResetOption("✉", "Send via Email", "Seamlessly reset your password via email address.", onEmailSelected)
        ResetOption("♙", "Send via 2FA", "Seamlessly reset your password via 2 Factors.", {})
        ResetOption("●", "Send via Google Auth", "Seamlessly reset your password via Google Auth.", onGoogleSelected)
        MovaButton("Reset Password   →", onClick = onEmailSelected)
    }
}

@Composable
private fun ResetOption(icon: String, title: String, body: String, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = Modifier.fillMaxWidth().height(64.dp)) {
        Text("$icon   $title\n$body")
    }
}
