package co.edu.unal.dadm.mova.presentation.navigation

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import co.edu.unal.dadm.mova.core.di.NutritionContainer
import co.edu.unal.dadm.mova.domain.model.MealType
import co.edu.unal.dadm.mova.presentation.screen.nutrition.AddMealScreen
import co.edu.unal.dadm.mova.presentation.screen.nutrition.CalorieIntakeScreen
import co.edu.unal.dadm.mova.presentation.screen.nutrition.CalorieStatsScreen
import co.edu.unal.dadm.mova.presentation.screen.nutrition.FoodAddedScreen
import co.edu.unal.dadm.mova.presentation.screen.nutrition.MealDetailScreen
import co.edu.unal.dadm.mova.presentation.screen.nutrition.MealSuggestionScreen
import co.edu.unal.dadm.mova.presentation.screen.nutrition.MyMealsScreen
import java.time.LocalDate

/** Destinos de Nutrición. Cada pantalla agrega aquí su composable (Tasks 12–17). */
fun NavGraphBuilder.nutritionGraph(navController: NavController, container: NutritionContainer) {
    composable(Routes.NutritionExplore) {
        MealSuggestionScreen(
            container = container,
            onBack = { navController.popBackStack() },
            onMeal = { mealId -> navController.navigate(Routes.nutritionMeal(mealId)) }
        )
    }
    composable(Routes.NutritionStats) {
        CalorieStatsScreen(container = container, onBack = { navController.popBackStack() })
    }
    composable(Routes.NutritionIntake) {
        CalorieIntakeScreen(
            container = container,
            onBack = { navController.popBackStack() },
            onMyMeals = { navController.navigate(Routes.nutritionMyMeals()) },
            onAddMeal = { navController.navigate(Routes.nutritionAdd()) },
            onExplore = { navController.navigate(Routes.NutritionExplore) },
            onStats = { navController.navigate(Routes.NutritionStats) }
        )
    }
    composable(
        Routes.NutritionMeal,
        arguments = listOf(navArgument("mealId") { type = NavType.StringType }, optionalDateArg)
    ) { entry ->
        MealDetailScreen(
            container = container,
            mealId = entry.arguments?.getString("mealId").orEmpty(),
            date = entry.dateArg(),
            onBack = { navController.popBackStack() },
            onAdded = { date ->
                navController.navigate(Routes.nutritionAdded(date)) {
                    popUpTo(Routes.NutritionMeal) { inclusive = true }
                }
            }
        )
    }
    composable(Routes.NutritionMyMeals, arguments = listOf(optionalDateArg)) { entry ->
        MyMealsScreen(
            container = container,
            initialDate = entry.dateArg(),
            onBack = { navController.popBackStack() },
            onAddMeal = { type, date -> navController.navigate(Routes.nutritionAdd(type, date)) },
            onExplore = { navController.navigate(Routes.NutritionExplore) }
        )
    }
    composable(Routes.NutritionAdded, arguments = listOf(optionalDateArg)) { entry ->
        val date = entry.dateArg()
        FoodAddedScreen(onBackToMyMeals = {
            // Si My Meals ya está en la pila (flujo desde ahí), se descarta junto con lo que hay encima
            // y se recrea con la fecha elegida: queda exactamente una entrada. Si no (flujo desde Explore),
            // solo se retira "Food Added" y se apila una.
            val myMealsInStack = runCatching { navController.getBackStackEntry(Routes.NutritionMyMeals) }.isSuccess
            navController.navigate(Routes.nutritionMyMeals(date)) {
                popUpTo(if (myMealsInStack) Routes.NutritionMyMeals else Routes.NutritionAdded) { inclusive = true }
                launchSingleTop = true
            }
        })
    }
    composable(
        Routes.NutritionAdd,
        arguments = listOf(
            navArgument("type") { type = NavType.StringType; nullable = true; defaultValue = null },
            optionalDateArg
        )
    ) { entry ->
        AddMealScreen(
            container = container,
            date = entry.dateArg(),
            initialType = entry.arguments?.getString("type")?.let { runCatching { MealType.valueOf(it) }.getOrNull() },
            onBack = { navController.popBackStack() },
            onAdded = { date ->
                navController.navigate(Routes.nutritionAdded(date)) {
                    popUpTo(Routes.NutritionAdd) { inclusive = true }
                }
            }
        )
    }
}

internal val optionalDateArg = navArgument("date") {
    type = NavType.StringType
    nullable = true
    defaultValue = null
}

internal fun NavBackStackEntry.dateArg(): LocalDate =
    arguments?.getString("date")?.let(LocalDate::parse) ?: LocalDate.now()
