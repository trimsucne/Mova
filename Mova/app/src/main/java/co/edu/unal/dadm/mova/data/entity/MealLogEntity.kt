package co.edu.unal.dadm.mova.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "meal_logs", indices = [Index(value = ["userId", "date"])])
data class MealLogEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val date: String,
    val mealType: String,
    val source: String,
    val mealId: String?,
    val name: String,
    val grams: Int?,
    val proteinG: Double,
    val carbsG: Double,
    val fatG: Double,
    val kcal: Int,
    val createdAt: Long,
    val updatedAt: Long,
    val deleted: Boolean,
    val syncState: String
)
