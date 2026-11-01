package co.edu.unal.dadm.mova.presentation.screen.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private val introPages = listOf(
    "Move with a plan" to "Build flexible routines around your real day.",
    "Track healthy habits" to "Keep progress visible across activity, food, water, and rest.",
    "Adapt and continue" to "A difficult day changes your plan; it does not end your progress."
)

@Composable
fun IntroPagerScreen(onFinished: () -> Unit) {
    var page by remember { mutableIntStateOf(0) }
    val (title, body) = introPages[page]
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("MOVA")
        LinearProgressIndicator(
            progress = { (page + 1).toFloat() / introPages.size },
            modifier = Modifier.fillMaxWidth()
        )
        Text(title)
        Text(body)
        Button(
            onClick = {
                if (page == introPages.lastIndex) onFinished() else page++
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (page == introPages.lastIndex) "Continue" else "Next")
        }
    }
}
