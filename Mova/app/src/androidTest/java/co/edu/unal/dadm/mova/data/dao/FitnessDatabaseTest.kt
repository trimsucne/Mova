package co.edu.unal.dadm.mova.data.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import co.edu.unal.dadm.mova.data.database.FitnessDatabase
import co.edu.unal.dadm.mova.data.repository.ActivityRepositoryImpl
import co.edu.unal.dadm.mova.data.repository.WorkoutRepositoryImpl
import co.edu.unal.dadm.mova.domain.model.ActivityLog
import co.edu.unal.dadm.mova.domain.model.ActivityType
import co.edu.unal.dadm.mova.domain.model.RoutePoint
import co.edu.unal.dadm.mova.domain.model.WorkoutCategory
import co.edu.unal.dadm.mova.domain.model.WorkoutSeed
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** Prueba de Room en memoria: requiere dispositivo/emulador (`connectedDebugAndroidTest`). */
@RunWith(AndroidJUnit4::class)
class FitnessDatabaseTest {
    private lateinit var db: FitnessDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, FitnessDatabase::class.java).build()
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun seedIfEmpty_es_idempotente_y_carga_workouts_con_ejercicios() = runBlocking {
        db.seedIfEmpty()
        db.seedIfEmpty()
        val repo = WorkoutRepositoryImpl(db.workoutDao(), db.workoutSessionDao())
        val strength = repo.observeByCategory(WorkoutCategory.Strength).first()
        assertEquals(WorkoutSeed.workouts.count { it.category == WorkoutCategory.Strength }, strength.size)
        val back = repo.observeWorkout("back").first()
        assertNotNull(back)
        assertEquals(WorkoutSeed.workouts.first { it.id == "back" }, back)
    }

    @Test
    fun activityLog_persiste_con_su_ruta() = runBlocking {
        val repo = ActivityRepositoryImpl(db.activityLogDao())
        val log = ActivityLog("x", ActivityType.Running, 10, 600, 1500.0, 114, null, listOf(RoutePoint(4.0, -74.0, 1)))
        repo.save(log)
        assertEquals(log, repo.observe("x").first())
        assertEquals(listOf(log), repo.observeAll().first())
    }
}
