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
import androidx.compose.runtime.remember
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.unal.dadm.mova.R
import co.edu.unal.dadm.mova.domain.repository.AuthRepository
import co.edu.unal.dadm.mova.presentation.screen.auth.components.SocialLoginRow
import co.edu.unal.dadm.mova.presentation.viewmodel.auth.AuthViewModelFactory
import co.edu.unal.dadm.mova.presentation.viewmodel.auth.SignUpViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import androidx.activity.result.contract.ActivityResultContracts

@Composable
fun SignUpScreen(repository: AuthRepository, onBack: () -> Unit, onSignIn: () -> Unit, onAuthenticated: () -> Unit) {
    val viewModel: SignUpViewModel = viewModel(factory = AuthViewModelFactory(repository))
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = androidx.compose.ui.platform.LocalContext.current
    val googleSignInClient = remember(context) {
        GoogleSignIn.getClient(
            context,
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
        )
    }
    val googleLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode != android.app.Activity.RESULT_OK) {
            viewModel.googleSignInCancelled()
            return@rememberLauncherForActivityResult
        }
        try {
            val account = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                .getResult(ApiException::class.java)
            account.idToken?.let(viewModel::googleSignIn)
                ?: viewModel.googleSignInError("Google did not return an ID token")
        } catch (_: ApiException) {
            viewModel.googleSignInError("Google sign-in failed")
        }
    }
    LaunchedEffect(state.completed) {
        if (state.completed) onAuthenticated()
    }
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Sign Up For Free", style = MaterialTheme.typography.headlineMedium)
        OutlinedTextField(state.email, viewModel::emailChanged, label = { Text("Email Address") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(state.password, viewModel::passwordChanged, label = { Text("Password") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
        OutlinedTextField(state.confirmation, viewModel::confirmationChanged, label = { Text("Confirm Password") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
        state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Button(onClick = viewModel::signUp, enabled = !state.isLoading, modifier = Modifier.fillMaxWidth()) {
            if (state.isLoading) CircularProgressIndicator() else Text("Sign Up")
        }
        SocialLoginRow(!state.isLoading) { googleLauncher.launch(googleSignInClient.signInIntent) }
        TextButton(onClick = onSignIn) { Text("Already have an account? Sign In") }
        TextButton(onClick = onBack) { Text("Back") }
    }
}
