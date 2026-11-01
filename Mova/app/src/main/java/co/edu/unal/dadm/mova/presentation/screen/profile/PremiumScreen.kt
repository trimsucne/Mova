package co.edu.unal.dadm.mova.presentation.screen.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PremiumScreen(
    onPurchase: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Mova Premium")
        Text("Unlock adaptive recommendations, detailed statistics, AI assistant, and ad-free use.")
        Button(onClick = onPurchase) { Text("Continue to purchase") }
        Button(onClick = onBack) { Text("Back") }
    }
}
