package co.edu.unal.dadm.mova.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import co.edu.unal.dadm.mova.presentation.theme.MovaTheme as PresentationMovaTheme

@Composable
fun MovaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    PresentationMovaTheme(
        darkTheme = darkTheme,
        content = content
    )
}
