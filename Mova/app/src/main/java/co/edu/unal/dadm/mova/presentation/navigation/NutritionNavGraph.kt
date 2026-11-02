package co.edu.unal.dadm.mova.presentation.navigation

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.navArgument
import co.edu.unal.dadm.mova.core.di.NutritionContainer
import java.time.LocalDate

/** Destinos de Nutrición. Cada pantalla agrega aquí su composable (Tasks 12–17). */
fun NavGraphBuilder.nutritionGraph(navController: NavController, container: NutritionContainer) {
}

internal val optionalDateArg = navArgument("date") {
    type = NavType.StringType
    nullable = true
    defaultValue = null
}

internal fun NavBackStackEntry.dateArg(): LocalDate =
    arguments?.getString("date")?.let(LocalDate::parse) ?: LocalDate.now()
