package co.edu.unal.dadm.mova.presentation.screen.auth

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.unal.dadm.mova.R
import co.edu.unal.dadm.mova.domain.repository.AuthRepository
import co.edu.unal.dadm.mova.presentation.screen.auth.components.AuthField
import co.edu.unal.dadm.mova.presentation.screen.auth.components.AuthHeader
import co.edu.unal.dadm.mova.presentation.screen.auth.components.MovaButton
import co.edu.unal.dadm.mova.presentation.screen.auth.components.SocialLoginRow
import co.edu.unal.dadm.mova.presentation.viewmodel.auth.AuthViewModelFactory
import co.edu.unal.dadm.mova.presentation.viewmodel.auth.SignInViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

@Composable
fun SignInScreen(repository: AuthRepository, onBack: () -> Unit, onSignUp: () -> Unit, onForgotPassword: () -> Unit, onAuthenticated: () -> Unit) {
    val viewModel: SignInViewModel = viewModel(factory = AuthViewModelFactory(repository))
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = androidx.compose.ui.platform.LocalContext.current
    val client = remember(context) {
        GoogleSignIn.getClient(context, GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id)).requestEmail().build())
    }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode != Activity.RESULT_OK) {
            viewModel.googleSignInCancelled()
        } else {
            try {
                val account = GoogleSignIn.getSignedInAccountFromIntent(result.data).getResult(ApiException::class.java)
                account.idToken?.let(viewModel::googleSignIn) ?: viewModel.googleSignInError("Google did not return an ID token")
            } catch (_: ApiException) {
                viewModel.googleSignInError("Google sign-in failed")
            }
        }
    }
    LaunchedEffect(state.completed) { if (state.completed) onAuthenticated() }
    Column(Modifier.fillMaxSize().padding(horizontal = 10.dp, vertical = 24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        AuthHeader("Sign In To Mova", "Let's personalize your fitness with AI", onBack)
        AuthField(state.email, viewModel::emailChanged, "Email Address")
        AuthField(state.password, viewModel::passwordChanged, "Password", PasswordVisualTransformation())
        state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        MovaButton("Sign In   →", !state.isLoading, viewModel::signIn)
        if (state.isLoading) CircularProgressIndicator()
        SocialLoginRow(!state.isLoading) { launcher.launch(client.signInIntent) }
        TextButton(onClick = onForgotPassword) { Text("Forgot Password") }
        TextButton(onClick = onSignUp) { Text("Don't have an account? Sign Up") }
    }
}
