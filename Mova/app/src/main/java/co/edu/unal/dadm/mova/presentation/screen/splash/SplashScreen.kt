package co.edu.unal.dadm.mova.presentation.screen.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import co.edu.unal.dadm.mova.presentation.screen.auth.components.MovaLogo
import co.edu.unal.dadm.mova.presentation.theme.MovaOrange
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(Unit) {
        delay(1800)
        onSplashFinished()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MovaOrange),
        contentAlignment = Alignment.Center
    ) {
        MovaLogo(inverse = true)
    }
}
