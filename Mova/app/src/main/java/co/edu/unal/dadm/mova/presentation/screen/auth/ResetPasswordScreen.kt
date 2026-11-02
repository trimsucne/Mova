package co.edu.unal.dadm.mova.presentation.screen.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.unal.dadm.mova.domain.repository.AuthRepository
import co.edu.unal.dadm.mova.presentation.components.MovaPrimaryButton
import co.edu.unal.dadm.mova.presentation.components.MovaTextField
import co.edu.unal.dadm.mova.presentation.components.TopBarBack
import co.edu.unal.dadm.mova.presentation.theme.MovaInk
import co.edu.unal.dadm.mova.presentation.theme.MovaMuted
import co.edu.unal.dadm.mova.presentation.viewmodel.auth.AuthViewModelFactory
import co.edu.unal.dadm.mova.presentation.viewmodel.auth.ResetPasswordViewModel

@Composable
fun ResetPasswordScreen(
    repository: AuthRepository,
    onBack: () -> Unit,
    onSent: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: ResetPasswordViewModel = viewModel(factory = AuthViewModelFactory(repository))
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.sent) {
        if (state.sent) onSent()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TopBarBack(onBack = onBack)

        Text(
            text = "Reset Password",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MovaInk
        )

        Text(
            text = "Enter email address to receive password reset link.",
            fontSize = 14.sp,
            color = MovaMuted
        )

        Spacer(modifier = Modifier.height(12.dp))

        MovaTextField(
            value = state.email,
            onValueChange = viewModel::emailChanged,
            label = "Email Address",
            placeholder = "elementary221b@gmail.com",
            leadingIconText = "✉"
        )

        state.error?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        MovaPrimaryButton(
            text = "Send Reset Email",
            onClick = viewModel::sendReset,
            enabled = !state.isLoading,
            containerColor = MovaInk,
            contentColor = Color.White
        )

        if (state.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        }
    }
}
