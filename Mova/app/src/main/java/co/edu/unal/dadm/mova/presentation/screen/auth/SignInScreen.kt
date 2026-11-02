package co.edu.unal.dadm.mova.presentation.screen.auth

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.unal.dadm.mova.R
import co.edu.unal.dadm.mova.domain.repository.AuthRepository
import co.edu.unal.dadm.mova.presentation.components.MovaPrimaryButton
import co.edu.unal.dadm.mova.presentation.components.MovaTextField
import co.edu.unal.dadm.mova.presentation.screen.auth.components.AuthHeader
import co.edu.unal.dadm.mova.presentation.screen.auth.components.SocialLoginRow
import co.edu.unal.dadm.mova.presentation.theme.MovaInk
import co.edu.unal.dadm.mova.presentation.theme.MovaOrange
import co.edu.unal.dadm.mova.presentation.viewmodel.auth.AuthViewModelFactory
import co.edu.unal.dadm.mova.presentation.viewmodel.auth.SignInViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

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
    val context = LocalContext.current
    var passwordVisible by remember { mutableStateOf(false) }

    val client = remember(context) {
        GoogleSignIn.getClient(
            context,
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
        )
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

    LaunchedEffect(state.completed) {
        if (state.completed) onAuthenticated()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AuthHeader(
            title = "Sign In To Mova",
            subtitle = "Let's personalize your fitness with AI",
            onBack = onBack
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MovaTextField(
                value = state.email,
                onValueChange = viewModel::emailChanged,
                label = "Email Address",
                placeholder = "elementary221b@gmail.com",
                leadingIconText = "✉"
            )

            MovaTextField(
                value = state.password,
                onValueChange = viewModel::passwordChanged,
                label = "Password",
                placeholder = "••••••••••••",
                leadingIconText = "🔒",
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Text(if (passwordVisible) "👁️" else "👁️‍🗨️", fontSize = 16.sp)
                    }
                }
            )

            state.error?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            MovaPrimaryButton(
                text = "Sign In",
                onClick = viewModel::signIn,
                enabled = !state.isLoading,
                containerColor = MovaInk,
                contentColor = Color.White
            )

            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }

            Spacer(modifier = Modifier.height(12.dp))

            SocialLoginRow(
                enabled = !state.isLoading,
                onGoogleClick = { launcher.launch(client.signInIntent) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(text = "Don't have an account? ", fontSize = 14.sp, color = Color.Gray)
                Text(
                    text = "Sign Up.",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MovaOrange,
                    modifier = Modifier.clickable(onClick = onSignUp)
                )
            }

            Text(
                text = "Forgot Password",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MovaOrange,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onForgotPassword)
                    .padding(top = 4.dp)
            )
        }
    }
}
