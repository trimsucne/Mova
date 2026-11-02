package co.edu.unal.dadm.mova.presentation.screen.auth

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import co.edu.unal.dadm.mova.R
import co.edu.unal.dadm.mova.presentation.components.ImagePlaceholder
import co.edu.unal.dadm.mova.presentation.components.SuccessDialog

@Composable
fun ResetSentScreen(
    onBackToSignIn: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // Metallic Padlock Background Image
        ImagePlaceholder(
            label = "Metallic Lock Security Background",
            drawableResId = R.drawable.img_metallic_lock,
            modifier = Modifier.fillMaxSize(),
            backgroundColor = Color(0xFF2C2C2E)
        )

        // Overlay dialog
        SuccessDialog(
            title = "Password Sent!",
            message = "We've sent the password to **221b@gmail.com. Resend if the password is not received! 🔥",
            buttonText = "Re-Send Password 🔒",
            onButtonClick = onBackToSignIn,
            onClose = onBackToSignIn
        )
    }
}
