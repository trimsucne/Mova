package co.edu.unal.dadm.mova.presentation.screen.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import co.edu.unal.dadm.mova.presentation.screen.auth.components.MovaLogo
import co.edu.unal.dadm.mova.ui.theme.MovaOrange

@Composable
fun WelcomeScreen(onGetStarted: () -> Unit, onSignIn: () -> Unit) {
    Column(
        Modifier.fillMaxSize().background(MovaOrange).padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        Spacer(Modifier.weight(1f))
        MovaLogo(inverse = true)
        Text("Your personal fitness AI assistant 🤖", color = Color.White)
        Text("Welcome To\nMOVA", color = Color.White, style = androidx.compose.material3.MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(20.dp))
        Button(
            onClick = onGetStarted,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = MovaOrange)
        ) { Text("Get Started   →") }
        androidx.compose.material3.TextButton(onClick = onSignIn) {
            Text("Already have account? Sign In", color = Color.White)
        }
    }
}
