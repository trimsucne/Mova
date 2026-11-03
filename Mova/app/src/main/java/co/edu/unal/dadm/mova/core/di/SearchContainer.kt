package co.edu.unal.dadm.mova.core.di

import android.content.Context
import co.edu.unal.dadm.mova.data.local.RecentSearchesDataSource
import co.edu.unal.dadm.mova.data.repository.SearchRepositoryImpl
import co.edu.unal.dadm.mova.domain.repository.RecentSearchesStore
import co.edu.unal.dadm.mova.domain.repository.WorkoutSearchSource
import co.edu.unal.dadm.mova.domain.usecase.SearchCatalogUseCase

/** Ensamblado manual de Búsqueda. Reutiliza el catálogo y el registro de comidas de Nutrición. */
class SearchContainer private constructor(context: Context) {
    private val nutrition = NutritionContainer.get(context)

    // Spec §12: cambiar NONE por el adaptador de observeWorkouts() cuando Entrenamiento lo entregue.
    private val workouts: WorkoutSearchSource = WorkoutSearchSource.NONE

    val repository = SearchRepositoryImpl(nutrition.mealRepository, workouts)
    val searchCatalog = SearchCatalogUseCase(repository)
    val recentSearches: RecentSearchesStore = RecentSearchesDataSource.create(context)
    val workoutsAvailable: Boolean = workouts.isAvailable

    val mealRepository = nutrition.mealRepository
    val logMeal = nutrition.logMeal
    val currentUserId = nutrition.currentUserId

    companion object {
        @Volatile
        private var instance: SearchContainer? = null

        fun get(context: Context): SearchContainer =
            instance ?: synchronized(this) {
                instance ?: SearchContainer(context.applicationContext).also { instance = it }
            }
    }
}
