package co.edu.unal.dadm.mova.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import co.edu.unal.dadm.mova.data.dao.MealDao
import co.edu.unal.dadm.mova.data.dao.MealLogDao
import co.edu.unal.dadm.mova.data.dao.UserDao
import co.edu.unal.dadm.mova.data.dao.UserProfileDao
import co.edu.unal.dadm.mova.data.entity.MealEntity
import co.edu.unal.dadm.mova.data.entity.MealLogEntity

@Database(entities = [MealEntity::class, MealLogEntity::class], version = 1, exportSchema = true)
abstract class MovaDatabase : RoomDatabase() {
    abstract fun mealDao(): MealDao
    abstract fun mealLogDao(): MealLogDao

    // Simulaciones de Perfil; se conservan hasta que ese módulo migre a Room.
    fun userDao(): UserDao = UserDao()
    fun userProfileDao(): UserProfileDao = UserProfileDao()

    companion object {
        @Volatile
        private var instance: MovaDatabase? = null

        fun getInstance(context: Context): MovaDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(context.applicationContext, MovaDatabase::class.java, "mova.db")
                    .addCallback(DatabaseSeeder(context.applicationContext))
                    .build()
                    .also { instance = it }
            }
    }
}
