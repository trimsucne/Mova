package co.edu.unal.dadm.mova.testutil

import co.edu.unal.dadm.mova.domain.model.Diet
import co.edu.unal.dadm.mova.domain.model.Macros
import co.edu.unal.dadm.mova.domain.model.Meal
import co.edu.unal.dadm.mova.domain.model.MealCategory
import co.edu.unal.dadm.mova.domain.model.MealLog
import co.edu.unal.dadm.mova.domain.model.MealType
import java.time.Instant
import java.time.LocalDate

object TestMeals {
    val VEGAN = setOf(Diet.VEGAN, Diet.VEGETARIAN, Diet.CARNIVORE)
    val VEGETARIAN = setOf(Diet.VEGETARIAN, Diet.CARNIVORE)
    val MEAT = setOf(Diet.CARNIVORE)

    fun meal(
        id: String,
        name: String = "Meal $id",
        protein: Double = 10.0,
        carbs: Double = 10.0,
        fat: Double = 5.0,
        diets: Set<Diet> = MEAT,
        category: MealCategory = MealCategory.PROTEIN,
        suitableFor: Set<MealType> = setOf(MealType.LUNCH),
        servingGrams: Int = 100,
        tags: List<String> = emptyList()
    ): Meal {
        val macros = Macros(protein, carbs, fat)
        return Meal(id, name, category, suitableFor, diets, servingGrams, macros, macros.kcal, 10, id, tags)
    }

    fun log(
        id: String,
        date: LocalDate,
        protein: Double = 10.0,
        carbs: Double = 10.0,
        fat: Double = 5.0,
        mealType: MealType = MealType.LUNCH,
        userId: String = "u1"
    ) = MealLog.manual("Log $id", Macros(protein, carbs, fat), userId, date, mealType, Instant.EPOCH, id = id)
}
