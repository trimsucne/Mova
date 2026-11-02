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
import co.edu.unal.dadm.mova.presentation.screen.nutrition.FoodAddedScreen
import co.edu.unal.dadm.mova.presentation.screen.nutrition.MealDetailScreen
import co.edu.unal.dadm.mova.presentation.screen.nutrition.MyMealsScreen
import java.time.LocalDate

/** Destinos de Nutrición. Cada pantalla agrega aquí su composable (Tasks 12–17). */
fun NavGraphBuilder.nutritionGraph(navController: NavController, container: NutritionContainer) {
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
            navController.navigate(Routes.nutritionMyMeals(date)) {
                popUpTo(Routes.NutritionAdded) { inclusive = true }
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
