package co.edu.unal.dadm.mova.presentation.screen.auth

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import co.edu.unal.dadm.mova.presentation.viewmodel.auth.SignUpViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

@Composable
fun SignUpScreen(repository: AuthRepository, onBack: () -> Unit, onSignIn: () -> Unit, onAuthenticated: () -> Unit) {
    val viewModel: SignUpViewModel = viewModel(factory = AuthViewModelFactory(repository))
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = androidx.compose.ui.platform.LocalContext.current
    val client = remember(context) {
        GoogleSignIn.getClient(context, GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id)).requestEmail().build())
    }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode != Activity.RESULT_OK) viewModel.googleSignInCancelled()
        else try {
            val account = GoogleSignIn.getSignedInAccountFromIntent(result.data).getResult(ApiException::class.java)
            account.idToken?.let(viewModel::googleSignIn) ?: viewModel.googleSignInError("Google did not return an ID token")
        } catch (_: ApiException) {
            viewModel.googleSignInError("Google sign-up failed")
        }
    }
    LaunchedEffect(state.completed) { if (state.completed) onAuthenticated() }
    Column(Modifier.fillMaxSize().padding(horizontal = 10.dp, vertical = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        AuthHeader("Sign Up For Free", "Quickly make your account in 1 minute", onBack)
        AuthField(state.email, viewModel::emailChanged, "Email Address")
        AuthField(state.password, viewModel::passwordChanged, "Password", PasswordVisualTransformation())
        AuthField(state.confirmation, viewModel::confirmationChanged, "Confirm Password", PasswordVisualTransformation(), state.error != null)
        state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        MovaButton("Sign Up   →", !state.isLoading, viewModel::signUp)
        SocialLoginRow(!state.isLoading) { launcher.launch(client.signInIntent) }
        TextButton(onClick = onSignIn) { Text("Already have an account? Sign In") }
    }
}
