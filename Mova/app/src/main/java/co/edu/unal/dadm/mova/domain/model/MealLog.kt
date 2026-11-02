package co.edu.unal.dadm.mova.domain.model

import java.time.Instant
import java.time.LocalDate
import java.util.UUID

data class MealLog(
    val id: String,
    val userId: String,
    val date: LocalDate,
    val mealType: MealType,
    val source: MealSource,
    val mealId: String?,
    val name: String,
    val grams: Int?,
    val macros: Macros,
    val kcal: Int,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    companion object {
        /** userId usado cuando no hay sesión; esos registros nunca se suben a Firestore. */
        const val LOCAL_USER_ID = "local"

        fun fromMeal(
            meal: Meal,
            grams: Int,
            userId: String,
            date: LocalDate,
            mealType: MealType,
            now: Instant,
            id: String = UUID.randomUUID().toString()
        ): MealLog {
            val macros = meal.macros.scaled(grams.toDouble() / meal.servingGrams)
            return MealLog(id, userId, date, mealType, MealSource.CATALOG, meal.id, meal.name, grams, macros, macros.kcal, now, now)
        }

        fun manual(
            name: String,
            macros: Macros,
            userId: String,
            date: LocalDate,
            mealType: MealType,
            now: Instant,
            id: String = UUID.randomUUID().toString()
        ) = MealLog(id, userId, date, mealType, MealSource.MANUAL, null, name.trim(), null, macros, macros.kcal, now, now)
    }
}
