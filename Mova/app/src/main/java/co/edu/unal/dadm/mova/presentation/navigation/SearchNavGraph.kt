package co.edu.unal.dadm.mova.presentation.navigation

import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import co.edu.unal.dadm.mova.core.di.SearchContainer
import co.edu.unal.dadm.mova.presentation.screen.home.SearchScreen

fun NavGraphBuilder.searchGraph(navController: NavController) {
    composable(
        Routes.Search,
        arguments = listOf(navArgument("q") { type = NavType.StringType; nullable = true; defaultValue = null })
    ) { entry ->
        val context = LocalContext.current
        val container = remember { SearchContainer.get(context) }
        SearchScreen(
            container = container,
            initialQuery = entry.arguments?.getString("q").orEmpty(),
            onBack = { navController.popBackStack() },
            onOpenMeal = { mealId -> navController.navigate(Routes.nutritionMeal(mealId)) }
        )
    }
}
