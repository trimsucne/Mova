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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.unal.dadm.mova.presentation.viewmodel.onboarding.IntroPagerViewModel

private val introPages = listOf(
    "Move with a plan" to "Build flexible routines around your real day.",
    "Track healthy habits" to "Keep progress visible across activity, food, water, and rest.",
    "Adapt and continue" to "A difficult day changes your plan; it does not end your progress."
)

@Composable
fun IntroPagerScreen(onFinished: () -> Unit) {
    val viewModel: IntroPagerViewModel = viewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val page = state.page
    val (title, body) = introPages[page]
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("MOVA")
        LinearProgressIndicator(
            progress = { (page + 1).toFloat() / state.pageCount },
            modifier = Modifier.fillMaxWidth()
        )
        Text(title)
        Text(body)
        Button(
            onClick = {
                if (state.isLastPage) onFinished() else viewModel.next()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (page == introPages.lastIndex) "Continue" else "Next")
        }
    }
}
