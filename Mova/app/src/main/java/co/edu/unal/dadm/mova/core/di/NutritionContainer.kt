package co.edu.unal.dadm.mova.core.di

import android.content.Context
import android.util.Log
import co.edu.unal.dadm.mova.core.work.MealLogSyncScheduler
import co.edu.unal.dadm.mova.data.database.MovaDatabase
import co.edu.unal.dadm.mova.data.remote.FirestoreMealLogDataSource
import co.edu.unal.dadm.mova.data.remote.firebaseAuthUserIds
import co.edu.unal.dadm.mova.data.repository.MealRepositoryImpl
import co.edu.unal.dadm.mova.data.repository.MealSyncCoordinator
import co.edu.unal.dadm.mova.data.repository.ProfileRepositoryImpl
import co.edu.unal.dadm.mova.domain.model.MealLog
import co.edu.unal.dadm.mova.domain.repository.BurnedKcalProvider
import co.edu.unal.dadm.mova.domain.repository.ProfileRepository
import co.edu.unal.dadm.mova.domain.usecase.CalculateCalorieGoalUseCase
import co.edu.unal.dadm.mova.domain.usecase.CalculateKcalFromMacrosUseCase
import co.edu.unal.dadm.mova.domain.usecase.LogMealUseCase
import co.edu.unal.dadm.mova.domain.usecase.ObserveDailyIntakeUseCase
import co.edu.unal.dadm.mova.domain.usecase.SuggestMealsUseCase
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/** Ensamblado manual del módulo de Nutrición (el proyecto no usa Hilt). Una instancia por proceso. */
class NutritionContainer private constructor(context: Context) {
    private val appScope = CoroutineScope(
        SupervisorJob() + Dispatchers.IO + CoroutineExceptionHandler { _, e -> Log.w("NutritionContainer", "Unhandled error in app scope", e) }
    )
    private val database = MovaDatabase.getInstance(context)

    val currentUserId: () -> String = { FirebaseAuth.getInstance().currentUser?.uid ?: MealLog.LOCAL_USER_ID }

    val mealRepository = MealRepositoryImpl(
        mealDao = database.mealDao(),
        logDao = database.mealLogDao(),
        remote = FirestoreMealLogDataSource(),
        scheduler = MealLogSyncScheduler(context),
        scope = appScope
    )
    val profileRepository: ProfileRepository = ProfileRepositoryImpl(context)

    val calculateKcal = CalculateKcalFromMacrosUseCase()
    val logMeal = LogMealUseCase(mealRepository)
    val observeDailyIntake = ObserveDailyIntakeUseCase(
        mealRepository, profileRepository, BurnedKcalProvider.NONE, CalculateCalorieGoalUseCase(), currentUserId
    )
    val suggestMeals = SuggestMealsUseCase()

    val syncCoordinator = MealSyncCoordinator(mealRepository, firebaseAuthUserIds(), appScope)

    companion object {
        @Volatile
        private var instance: NutritionContainer? = null

        fun get(context: Context): NutritionContainer =
            instance ?: synchronized(this) {
                instance ?: NutritionContainer(context.applicationContext).also { instance = it }
            }
    }
}
