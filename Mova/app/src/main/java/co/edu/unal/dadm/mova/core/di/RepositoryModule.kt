package co.edu.unal.dadm.mova.core.di

import android.content.Context
import co.edu.unal.dadm.mova.data.repository.fake.FakeHydrationRepository
import co.edu.unal.dadm.mova.data.repository.fake.FakeNotificationRepository
import co.edu.unal.dadm.mova.data.repository.fake.FakeProgressRepository
import co.edu.unal.dadm.mova.domain.repository.ActivityRepository
import co.edu.unal.dadm.mova.domain.repository.HydrationRepository
import co.edu.unal.dadm.mova.domain.repository.MealRepository
import co.edu.unal.dadm.mova.domain.repository.NotificationRepository
import co.edu.unal.dadm.mova.domain.repository.ProfileRepository
import co.edu.unal.dadm.mova.domain.repository.ProgressRepository
import co.edu.unal.dadm.mova.domain.repository.WorkoutRepository
import co.edu.unal.dadm.mova.domain.usecase.ObserveDailyIntakeUseCase

/**
 * Ensamblado manual (sin Hilt) de los repositorios que usan Home, Notificaciones y Uplift Score.
 * Todo lo de comidas y perfil sale de [NutritionContainer] (una sola base de datos y un solo MealRepository).
 * Notifications, Progress y Hydration son fakes en memoria hasta que sus módulos existan;
 * al implementarlos, solo se reemplaza la instancia aquí.
 */
object RepositoryModule {
    fun profileRepository(context: Context): ProfileRepository = NutritionContainer.get(context).profileRepository

    fun mealRepository(context: Context): MealRepository = NutritionContainer.get(context).mealRepository

    fun observeDailyIntake(context: Context): ObserveDailyIntakeUseCase = NutritionContainer.get(context).observeDailyIntake

    /** Temporary fake until the Hydration module is implemented. */
    val hydrationRepository: HydrationRepository by lazy { FakeHydrationRepository() }

    /** Temporary fake until the Progress/Uplift module is implemented. */
    val progressRepository: ProgressRepository by lazy { FakeProgressRepository() }

    /** Modulo Workouts (Dev C): catalogo y sesiones reales en Room. */
    val workoutRepository: WorkoutRepository get() = FitnessContainer.workoutRepository

    /** Modulo Actividades (Dev C): registros reales en Room. */
    val activityRepository: ActivityRepository get() = FitnessContainer.activityRepository

    /** Temporary fake until the Notifications module is implemented. */
    val notificationRepository: NotificationRepository by lazy { FakeNotificationRepository() }
}
