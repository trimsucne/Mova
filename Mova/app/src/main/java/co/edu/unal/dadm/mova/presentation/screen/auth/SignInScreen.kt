package co.edu.unal.dadm.mova.presentation.screen.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.unal.dadm.mova.domain.repository.AuthRepository
import co.edu.unal.dadm.mova.presentation.viewmodel.auth.AuthViewModelFactory
import co.edu.unal.dadm.mova.presentation.viewmodel.auth.SignInViewModel

@Composable
fun SignInScreen(
    repository: AuthRepository,
    onBack: () -> Unit,
    onSignUp: () -> Unit,
    onForgotPassword: () -> Unit,
    onAuthenticated: () -> Unit
) {
    val viewModel: SignInViewModel = viewModel(factory = AuthViewModelFactory(repository))
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(state.completed) {
        if (state.completed) onAuthenticated()
    }
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Sign In To Mova", style = MaterialTheme.typography.headlineMedium)
        OutlinedTextField(state.email, viewModel::emailChanged, label = { Text("Email Address") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(
            state.password, viewModel::passwordChanged,
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Button(onClick = viewModel::signIn, enabled = !state.isLoading, modifier = Modifier.fillMaxWidth()) {
            if (state.isLoading) CircularProgressIndicator() else Text("Sign In")
        }
        TextButton(onClick = onForgotPassword) { Text("Forgot Password") }
        TextButton(onClick = onSignUp) { Text("Don't have an account? Sign Up") }
        TextButton(onClick = onBack) { Text("Back") }
    }
}
