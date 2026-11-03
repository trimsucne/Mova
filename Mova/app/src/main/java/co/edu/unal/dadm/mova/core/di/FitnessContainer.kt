package co.edu.unal.dadm.mova.core.di

import android.content.Context
import androidx.room.Room
import co.edu.unal.dadm.mova.core.health.AdaptiveHealthDataSource
import co.edu.unal.dadm.mova.core.health.HealthDataSource
import co.edu.unal.dadm.mova.data.database.FitnessDatabase
import co.edu.unal.dadm.mova.data.remote.OsrmDirectionsRepository
import co.edu.unal.dadm.mova.data.repository.ActivityRepositoryImpl
import co.edu.unal.dadm.mova.data.repository.PlacesRepositoryImpl
import co.edu.unal.dadm.mova.data.repository.WorkoutRepositoryImpl
import co.edu.unal.dadm.mova.data.tracking.TrackingRepositoryImpl
import co.edu.unal.dadm.mova.data.tracking.TrackingSession
import co.edu.unal.dadm.mova.domain.repository.ActivityRepository
import co.edu.unal.dadm.mova.domain.repository.DirectionsRepository
import co.edu.unal.dadm.mova.domain.repository.PlacesRepository
import co.edu.unal.dadm.mova.domain.repository.TrackingRepository
import co.edu.unal.dadm.mova.domain.repository.WorkoutRepository
import co.edu.unal.dadm.mova.domain.usecase.CompleteWorkoutSessionUseCase
import co.edu.unal.dadm.mova.domain.usecase.LogActivityUseCase
import co.edu.unal.dadm.mova.domain.usecase.ObserveWorkoutsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * DI manual (el proyecto aun no usa Hilt) para los modulos Workouts y Actividades.
 * Se inicializa en [co.edu.unal.dadm.mova.MovaApp]. Persistencia con Room ([FitnessDatabase]).
 */
object FitnessContainer {
    private lateinit var appContext: Context
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun init(context: Context) {
        appContext = context.applicationContext
        // Siembra el catalogo en segundo plano; los Flow de Room emiten cuando termina.
        appScope.launch { database.seedIfEmpty() }
    }

    val database: FitnessDatabase by lazy {
        Room.databaseBuilder(appContext, FitnessDatabase::class.java, "fitness.db").build()
    }

    val workoutRepository: WorkoutRepository by lazy {
        WorkoutRepositoryImpl(database.workoutDao(), database.workoutSessionDao())
    }
    val activityRepository: ActivityRepository by lazy { ActivityRepositoryImpl(database.activityLogDao()) }
    val placesRepository: PlacesRepository by lazy { PlacesRepositoryImpl(database.gymDao()) }
    val directionsRepository: DirectionsRepository by lazy { OsrmDirectionsRepository() }
    val healthDataSource: HealthDataSource by lazy { AdaptiveHealthDataSource(appContext) }

    val trackingSession: TrackingSession by lazy { TrackingSession() }
    val trackingRepository: TrackingRepository by lazy {
        TrackingRepositoryImpl(appContext, trackingSession)
    }

    val observeWorkouts by lazy { ObserveWorkoutsUseCase(workoutRepository) }
    val completeWorkoutSession by lazy { CompleteWorkoutSessionUseCase(workoutRepository) }
    val logActivity by lazy { LogActivityUseCase(activityRepository) }
}
