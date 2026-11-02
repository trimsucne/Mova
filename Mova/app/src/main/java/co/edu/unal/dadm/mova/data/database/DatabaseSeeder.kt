package co.edu.unal.dadm.mova.data.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import co.edu.unal.dadm.mova.data.entity.MealEntity

/** Carga assets/seed/meals.json la primera vez que se crea la base. */
class DatabaseSeeder(private val context: Context) : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        val json = context.assets.open("seed/meals.json").bufferedReader().use { it.readText() }
        db.beginTransaction()
        try {
            MealSeedParser.parse(json).forEach { meal ->
                db.insert("meals", SQLiteDatabase.CONFLICT_REPLACE, meal.toContentValues())
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    private fun MealEntity.toContentValues() = ContentValues().apply {
        put("id", id)
        put("name", name)
        put("category", category)
        put("suitableFor", suitableFor)
        put("dietTags", dietTags)
        put("servingGrams", servingGrams)
        put("proteinG", proteinG)
        put("carbsG", carbsG)
        put("fatG", fatG)
        put("kcal", kcal)
        put("prepMinutes", prepMinutes)
        put("imageRes", imageRes)
        put("tags", tags)
    }
}
