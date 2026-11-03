package co.edu.unal.dadm.mova.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.withTransaction
import co.edu.unal.dadm.mova.data.dao.ActivityLogDao
import co.edu.unal.dadm.mova.data.dao.GymDao
import co.edu.unal.dadm.mova.data.dao.WorkoutDao
import co.edu.unal.dadm.mova.data.dao.WorkoutExerciseDao
import co.edu.unal.dadm.mova.data.dao.WorkoutSessionDao
import co.edu.unal.dadm.mova.data.entity.ActivityLogEntity
import co.edu.unal.dadm.mova.data.entity.GymEntity
import co.edu.unal.dadm.mova.data.entity.WorkoutEntity
import co.edu.unal.dadm.mova.data.entity.WorkoutExerciseEntity
import co.edu.unal.dadm.mova.data.entity.WorkoutSessionEntity
import co.edu.unal.dadm.mova.data.mapper.toEntities
import co.edu.unal.dadm.mova.data.mapper.toEntity
import co.edu.unal.dadm.mova.data.repository.InMemoryPlacesRepository
import co.edu.unal.dadm.mova.domain.model.WorkoutSeed

/**
 * Base Room de los modulos Workouts y Actividades (Dev C). Se mantiene separada de
 * [MovaDatabase] (perfil/usuario, Dev A) para no acoplar los modulos.
 */
@Database(
    entities = [
        WorkoutEntity::class,
        WorkoutExerciseEntity::class,
        WorkoutSessionEntity::class,
        ActivityLogEntity::class,
        GymEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class FitnessDatabase : RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao
    abstract fun workoutExerciseDao(): WorkoutExerciseDao
    abstract fun workoutSessionDao(): WorkoutSessionDao
    abstract fun activityLogDao(): ActivityLogDao
    abstract fun gymDao(): GymDao

    /** Siembra el catalogo (workouts y gimnasios) solo si las tablas estan vacias. */
    suspend fun seedIfEmpty() {
        withTransaction {
            if (workoutDao().count() == 0) {
                val rows = WorkoutSeed.workouts.map { it.toEntities() }
                workoutDao().insertAll(rows.map { it.first })
                workoutExerciseDao().insertAll(rows.flatMap { it.second })
            }
            if (gymDao().count() == 0) {
                gymDao().insertAll(InMemoryPlacesRepository.SAMPLE_GYMS.map { it.toEntity() })
            }
        }
    }
}
