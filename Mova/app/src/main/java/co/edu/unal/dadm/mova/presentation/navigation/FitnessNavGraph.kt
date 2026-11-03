package co.edu.unal.dadm.mova.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import co.edu.unal.dadm.mova.domain.model.ActivityType
import co.edu.unal.dadm.mova.domain.model.WorkoutCategory
import co.edu.unal.dadm.mova.presentation.screen.activity.ActivitySummaryRoute
import co.edu.unal.dadm.mova.presentation.screen.activity.ActivityStatusRoute
import co.edu.unal.dadm.mova.presentation.screen.activity.ActivityTrackRoute
import co.edu.unal.dadm.mova.presentation.screen.activity.AddActivityRoute
import co.edu.unal.dadm.mova.presentation.screen.activity.HeartRateRoute
import co.edu.unal.dadm.mova.presentation.screen.fitness.FitnessHubScreen
import co.edu.unal.dadm.mova.presentation.screen.places.DirectionsRoute
import co.edu.unal.dadm.mova.presentation.screen.places.NearbyGymsRoute
import co.edu.unal.dadm.mova.presentation.screen.workout.StrengthCategoryRoute
import co.edu.unal.dadm.mova.presentation.screen.workout.WorkoutDetailRoute
import co.edu.unal.dadm.mova.presentation.screen.workout.WorkoutSessionRoute
import co.edu.unal.dadm.mova.presentation.screen.workout.WorkoutSummaryRoute

private fun stringArg(name: String) = navArgument(name) { type = NavType.StringType }

/** Destinos de Workouts (modulo 4) y Actividades/Tracking/Mapas (modulo 6) - Dev C. */
fun NavGraphBuilder.fitnessGraph(navController: NavHostController) {
    composable(FitnessRoutes.Hub) {
        FitnessHubScreen(
            onWorkouts = { navController.navigate(FitnessRoutes.workoutCategory(WorkoutCategory.STRENGTH.id)) },
            onActivities = { navController.navigate(FitnessRoutes.ActivityStatus) },
            onHeartRate = { navController.navigate(FitnessRoutes.HeartRate) },
            onGyms = { navController.navigate(FitnessRoutes.NearbyGyms) },
            onBack = { navController.popBackStack() }
        )
    }

    // ---- Workouts ----
    composable(FitnessRoutes.WorkoutCategory, arguments = listOf(stringArg("category"))) { entry ->
        StrengthCategoryRoute(
            category = WorkoutCategory.fromId(entry.arguments?.getString("category")),
            onWorkoutClick = { navController.navigate(FitnessRoutes.workoutDetail(it)) },
            onBack = { navController.popBackStack() }
        )
    }
    composable(FitnessRoutes.WorkoutDetail, arguments = listOf(stringArg("workoutId"))) { entry ->
        val id = entry.arguments?.getString("workoutId").orEmpty()
        WorkoutDetailRoute(
            workoutId = id,
            onStart = { navController.navigate(FitnessRoutes.workoutSession(id)) },
            onBack = { navController.popBackStack() }
        )
    }
    composable(FitnessRoutes.WorkoutSession, arguments = listOf(stringArg("workoutId"))) { entry ->
        val id = entry.arguments?.getString("workoutId").orEmpty()
        WorkoutSessionRoute(
            workoutId = id,
            onFinished = { sessionId ->
                navController.navigate(FitnessRoutes.workoutSummary(sessionId)) {
                    popUpTo(FitnessRoutes.workoutDetail(id)) { inclusive = true }
                }
            },
            onBack = { navController.popBackStack() }
        )
    }
    composable(FitnessRoutes.WorkoutSummary, arguments = listOf(stringArg("sessionId"))) { entry ->
        WorkoutSummaryRoute(
            sessionId = entry.arguments?.getString("sessionId").orEmpty(),
            onComplete = {
                if (!navController.popBackStack(FitnessRoutes.WorkoutCategory, inclusive = false)) {
                    navController.popBackStack()
                }
            }
        )
    }

    // ---- Actividades ----
    composable(FitnessRoutes.ActivityStatus) {
        ActivityStatusRoute(
            onAdd = { navController.navigate(FitnessRoutes.ActivityAdd) },
            onActivityClick = { navController.navigate(FitnessRoutes.activitySummary(it)) },
            onBack = { navController.popBackStack() }
        )
    }
    composable(FitnessRoutes.ActivityAdd) {
        AddActivityRoute(
            onContinue = { navController.navigate(FitnessRoutes.activityTrack(it.id)) },
            onBack = { navController.popBackStack() }
        )
    }
    composable(FitnessRoutes.ActivityTrack, arguments = listOf(stringArg("typeId"))) { entry ->
        val type = ActivityType.fromId(entry.arguments?.getString("typeId")) ?: ActivityType.Running
        ActivityTrackRoute(
            type = type,
            onSaved = { logId ->
                navController.navigate(FitnessRoutes.activitySummary(logId)) {
                    popUpTo(FitnessRoutes.ActivityAdd) { inclusive = true }
                }
            },
            onExit = { navController.popBackStack() }
        )
    }
    composable(FitnessRoutes.ActivitySummary, arguments = listOf(stringArg("logId"))) { entry ->
        ActivitySummaryRoute(
            logId = entry.arguments?.getString("logId").orEmpty(),
            onDone = {
                if (!navController.popBackStack(FitnessRoutes.ActivityStatus, inclusive = false)) {
                    navController.navigate(FitnessRoutes.ActivityStatus) {
                        popUpTo(FitnessRoutes.activitySummary(entry.arguments?.getString("logId").orEmpty())) { inclusive = true }
                    }
                }
            },
            onBack = { navController.popBackStack() }
        )
    }
    composable(FitnessRoutes.HeartRate) { HeartRateRoute(onBack = { navController.popBackStack() }) }

    // ---- Lugares / mapas ----
    composable(FitnessRoutes.NearbyGyms) {
        NearbyGymsRoute(
            onGymClick = { navController.navigate(FitnessRoutes.directions(it)) },
            onBack = { navController.popBackStack() }
        )
    }
    composable(FitnessRoutes.Directions, arguments = listOf(stringArg("gymId"))) { entry ->
        DirectionsRoute(
            gymId = entry.arguments?.getString("gymId").orEmpty(),
            onClose = { navController.popBackStack() }
        )
    }
}
