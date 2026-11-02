package co.edu.unal.dadm.mova.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Catálogo de solo lectura. Listas y enums se guardan como texto separado por comas. */
@Entity(tableName = "meals")
data class MealEntity(
    @PrimaryKey val id: String,
    val name: String,
    val category: String,
    val suitableFor: String,
    val dietTags: String,
    val servingGrams: Int,
    val proteinG: Double,
    val carbsG: Double,
    val fatG: Double,
    val kcal: Int,
    val prepMinutes: Int,
    val imageRes: String,
    val tags: String
)
