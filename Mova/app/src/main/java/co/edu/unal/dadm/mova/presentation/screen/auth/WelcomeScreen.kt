package co.edu.unal.dadm.mova.presentation.screen.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun WelcomeScreen(onGetStarted: () -> Unit, onSignIn: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Welcome to MOVA", style = MaterialTheme.typography.headlineLarge)
        Text("Your personal fitness AI assistant")
        Button(onClick = onGetStarted, modifier = Modifier.fillMaxWidth()) { Text("Get Started") }
        OutlinedButton(onClick = onSignIn, modifier = Modifier.fillMaxWidth()) {
            Text("Already have account? Sign In")
        }
    }
}
