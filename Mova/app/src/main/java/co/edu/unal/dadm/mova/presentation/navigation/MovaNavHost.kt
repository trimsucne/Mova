package co.edu.unal.dadm.mova.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import co.edu.unal.dadm.mova.domain.repository.AuthRepository
import co.edu.unal.dadm.mova.domain.repository.AssessmentRepository
import co.edu.unal.dadm.mova.presentation.screen.auth.ResetPasswordScreen
import co.edu.unal.dadm.mova.presentation.screen.auth.ResetMethodScreen
import co.edu.unal.dadm.mova.presentation.screen.auth.ResetSentScreen
import co.edu.unal.dadm.mova.presentation.screen.auth.SignInScreen
import co.edu.unal.dadm.mova.presentation.screen.auth.SignUpScreen
import co.edu.unal.dadm.mova.presentation.screen.auth.WelcomeScreen
import co.edu.unal.dadm.mova.presentation.screen.onboarding.IntroPagerScreen
import co.edu.unal.dadm.mova.presentation.screen.onboarding.AssessmentScreen
import co.edu.unal.dadm.mova.data.preferences.UserPreferencesImpl
import co.edu.unal.dadm.mova.data.repository.ProfileRepositoryImpl
import co.edu.unal.dadm.mova.presentation.screen.profile.ProfileScreen
import co.edu.unal.dadm.mova.presentation.screen.profile.AccountSettingsScreen
import androidx.compose.ui.platform.LocalContext

@Composable
fun MovaNavHost(authRepository: AuthRepository, assessmentRepository: AssessmentRepository) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val profileRepository = ProfileRepositoryImpl()
    val preferencesRepository = UserPreferencesImpl(context)
    NavHost(navController = navController, startDestination = Routes.Welcome) {
        composable(Routes.Welcome) {
            WelcomeScreen(
                onGetStarted = { navController.navigate(Routes.SignUp) },
                onSignIn = { navController.navigate(Routes.SignIn) }
            )
        }
        composable(Routes.SignIn) {
            SignInScreen(
                repository = authRepository,
                onBack = { navController.popBackStack() },
                onSignUp = { navController.navigate(Routes.SignUp) },
                onForgotPassword = { navController.navigate(Routes.ResetMethod) },
                onAuthenticated = { navController.navigate(Routes.Onboarding) { popUpTo(Routes.SignIn) { inclusive = true } } }
            )
        }
        composable(Routes.ResetMethod) {
            ResetMethodScreen(
                onEmailSelected = { navController.navigate(Routes.ResetPassword) },
                onGoogleSelected = { navController.navigate(Routes.ResetPassword) },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.SignUp) {
            SignUpScreen(
                repository = authRepository,
                onBack = { navController.popBackStack() },
                onSignIn = { navController.navigate(Routes.SignIn) },
                onAuthenticated = { navController.navigate(Routes.Onboarding) { popUpTo(Routes.SignUp) { inclusive = true } } }
            )
        }
        composable(Routes.ResetPassword) {
            ResetPasswordScreen(
                repository = authRepository,
                onBack = { navController.popBackStack() },
                onSent = { navController.navigate(Routes.ResetSent) }
            )
        }
        composable(Routes.ResetSent) {
            ResetSentScreen(onBackToSignIn = {
                navController.navigate(Routes.SignIn) {
                    popUpTo(Routes.Welcome)
                }
            })
        }
        composable(Routes.Onboarding) {
            IntroPagerScreen(onFinished = { navController.navigate(Routes.Assessment) })
        }
        composable(Routes.Assessment) {
            AssessmentScreen(
                authRepository = authRepository,
                assessmentRepository = assessmentRepository,
                onComplete = {
                navController.navigate(Routes.Main) {
                    popUpTo(Routes.Assessment) { inclusive = true }
                }
            })
        }
        composable(Routes.Main) {
            val userId = authRepository.currentUser.value?.id
            if (userId == null) {
                WelcomeScreen(
                    onGetStarted = { navController.navigate(Routes.SignUp) },
                    onSignIn = { navController.navigate(Routes.SignIn) }
                )
            } else {
                ProfileScreen(
                    repository = profileRepository,
                    userId = userId,
                    onSettings = { navController.navigate(Routes.Settings) },
                    onSignOut = {
                        authRepository.signOut()
                        navController.navigate(Routes.Welcome) {
                            popUpTo(Routes.Main) { inclusive = true }
                        }
                    }
                )
            }
        }
        composable(Routes.Profile) {
            val userId = authRepository.currentUser.value?.id
            if (userId != null) {
                ProfileScreen(profileRepository, userId, { navController.navigate(Routes.Settings) }, {
                    authRepository.signOut()
                    navController.navigate(Routes.Welcome) {
                        popUpTo(Routes.Welcome) { inclusive = true }
                    }
                })
            }
        }
        composable(Routes.Settings) {
            AccountSettingsScreen(preferencesRepository, onBack = { navController.popBackStack() })
        }
    }
}
