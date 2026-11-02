package co.edu.unal.dadm.mova.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import co.edu.unal.dadm.mova.core.di.NutritionContainer
import co.edu.unal.dadm.mova.data.preferences.UserPreferencesImpl
import co.edu.unal.dadm.mova.data.repository.ProfileRepositoryImpl
import co.edu.unal.dadm.mova.domain.repository.AssessmentRepository
import co.edu.unal.dadm.mova.domain.repository.AuthRepository
import co.edu.unal.dadm.mova.presentation.screen.auth.ResetMethodScreen
import co.edu.unal.dadm.mova.presentation.screen.auth.ResetPasswordScreen
import co.edu.unal.dadm.mova.presentation.screen.auth.ResetSentScreen
import co.edu.unal.dadm.mova.presentation.screen.auth.SignInScreen
import co.edu.unal.dadm.mova.presentation.screen.auth.SignUpScreen
import co.edu.unal.dadm.mova.presentation.screen.auth.WelcomeScreen
import co.edu.unal.dadm.mova.presentation.screen.home.HomeScreenRoute
import co.edu.unal.dadm.mova.presentation.screen.home.NotificationsScreenRoute
import co.edu.unal.dadm.mova.presentation.screen.onboarding.AssessmentScreen
import co.edu.unal.dadm.mova.presentation.screen.onboarding.AvatarStep
import co.edu.unal.dadm.mova.presentation.screen.onboarding.IntroPagerScreen
import co.edu.unal.dadm.mova.presentation.screen.profile.AccountSettingsScreen
import co.edu.unal.dadm.mova.presentation.screen.profile.CoachContactScreen
import co.edu.unal.dadm.mova.presentation.screen.profile.LinkedDevicesScreen
import co.edu.unal.dadm.mova.presentation.screen.profile.MainSecurityScreen
import co.edu.unal.dadm.mova.presentation.screen.profile.NotificationSettingsScreen
import co.edu.unal.dadm.mova.presentation.screen.profile.PersonalInfoScreen
import co.edu.unal.dadm.mova.presentation.screen.profile.PremiumScreen
import co.edu.unal.dadm.mova.presentation.screen.profile.ProfileScreen
import co.edu.unal.dadm.mova.presentation.screen.progress.UpliftScoreScreenRoute
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun MovaNavHost(authRepository: AuthRepository, assessmentRepository: AssessmentRepository) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val profileRepository = ProfileRepositoryImpl(context)
    val preferencesRepository = UserPreferencesImpl(context)
    val nutritionContainer = remember { NutritionContainer.get(context) }
    // Con sesión abierta se entra directo a Home; si no, a Welcome.
    val startDestination = remember { if (FirebaseAuth.getInstance().currentUser != null) Routes.Main else Routes.Welcome }
    val onNavigateTab: (String) -> Unit = { tab -> navController.navigateToTab(tab) }
    NavHost(navController = navController, startDestination = startDestination) {
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
                }
            )
        }
        composable(Routes.Main) {
            // Mismo userId que usa Nutrición para sus registros (uid de Firebase o "local").
            val userId = remember { nutritionContainer.currentUserId() }
            HomeScreenRoute(
                userId = userId,
                onNavigateToNotifications = { navController.navigate(Routes.Notifications) },
                onNavigateToSearch = { query -> navController.navigate(Routes.search(query)) { launchSingleTop = true } },
                onNavigateToScore = { navController.navigate(Routes.UpliftScore) },
                onNavigateToProfile = { navController.navigate(Routes.Profile) },
                onNavigateToTab = onNavigateTab,
                onNavigateToCalories = { navController.navigate(Routes.NutritionIntake) },
                onNavigateToMeals = { navController.navigate(Routes.NutritionExplore) }
            )
        }
        composable(Routes.Notifications) {
            val userId = remember { nutritionContainer.currentUserId() }
            NotificationsScreenRoute(userId = userId, onNavigateTab = onNavigateTab)
        }
        composable(Routes.UpliftScore) {
            val userId = remember { nutritionContainer.currentUserId() }
            UpliftScoreScreenRoute(userId = userId, onNavigateTab = onNavigateTab)
        }
        composable(Routes.Profile) {
            val userId = authRepository.currentUser.value?.id ?: "demo_user_id"
            ProfileScreen(
                repository = profileRepository,
                userId = userId,
                onSettings = { navController.navigate(Routes.Settings) },
                onAvatarPicker = { navController.navigate(Routes.AvatarPicker) },
                onSignOut = {
                    authRepository.signOut()
                    navController.navigate(Routes.Welcome) {
                        popUpTo(Routes.Welcome) { inclusive = true }
                    }
                },
                onPremiumFeature = { navController.navigate(Routes.Premium) }
            )
        }
        composable(Routes.AvatarPicker) {
            val userId = authRepository.currentUser.value?.id ?: "demo_user_id"
            AvatarStep(
                selected = "avatar_2",
                onSelected = { chosenAvatar ->
                    CoroutineScope(Dispatchers.IO).launch {
                        if (chosenAvatar.startsWith("content://")) {
                            val uri = android.net.Uri.parse(chosenAvatar)
                            val savedPath = profileRepository.saveAvatar(uri, "image/jpeg")
                            profileRepository.update(userId, mapOf("avatarPath" to savedPath))
                        } else {
                            profileRepository.update(userId, mapOf("avatarPath" to chosenAvatar))
                        }
                    }
                    navController.popBackStack()
                },
                onContinue = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.Settings) {
            AccountSettingsScreen(
                repository = preferencesRepository,
                onBack = { navController.popBackStack() },
                onNotifications = { navController.navigate(Routes.SettingsNotifications) },
                onPersonalInfo = { navController.navigate(Routes.SettingsPersonalInfo) },
                onCoachContact = { navController.navigate(Routes.SettingsCoachContact) },
                onLinkedDevices = { navController.navigate(Routes.SettingsLinkedDevices) },
                onMainSecurity = { navController.navigate(Routes.SettingsMainSecurity) }
            )
        }
        composable(Routes.SettingsNotifications) {
            NotificationSettingsScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.SettingsPersonalInfo) {
            val userId = authRepository.currentUser.value?.id ?: "demo_user_id"
            PersonalInfoScreen(
                repository = profileRepository,
                userId = userId,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.SettingsCoachContact) {
            CoachContactScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.SettingsLinkedDevices) {
            LinkedDevicesScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.SettingsMainSecurity) {
            MainSecurityScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.Premium) {
            PremiumScreen(
                onPurchase = { },
                onBack = { navController.popBackStack() }
            )
        }
        nutritionGraph(navController, nutritionContainer)
        searchGraph(navController)
    }
}

/** Pestañas de MovaBottomBar (PR #5). Home queda en la base de la pila; las demás no se apilan entre sí. */
private fun NavHostController.navigateToTab(tab: String) {
    val route = when (tab) {
        "home" -> Routes.Main
        "profile" -> Routes.Profile
        "notifications" -> Routes.Notifications
        "uplift_score" -> Routes.UpliftScore
        else -> return
    }
    navigate(route) {
        popUpTo(Routes.Main)
        launchSingleTop = true
    }
}
