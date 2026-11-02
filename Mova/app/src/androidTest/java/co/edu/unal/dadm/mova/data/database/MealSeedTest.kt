package co.edu.unal.dadm.mova.data.database

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import co.edu.unal.dadm.mova.domain.model.Macros
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MealSeedTest {
    private val meals by lazy {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        MealSeedParser.parse(context.assets.open("seed/meals.json").bufferedReader().use { it.readText() })
    }

    @Test
    fun hasFortyUniqueMeals() {
        assertEquals(40, meals.size)
        assertEquals(40, meals.map { it.id }.toSet().size)
    }

    @Test
    fun tenPerPrimaryMealType() {
        val counts = meals.groupingBy { it.suitableFor.substringBefore(',') }.eachCount()
        assertEquals(mapOf("BREAKFAST" to 10, "LUNCH" to 10, "DINNER" to 10, "SNACK" to 10), counts)
    }

    @Test
    fun enoughVeganAndVegetarianMeals() {
        assertTrue(meals.count { "VEGAN" in it.dietTags.split(',') } >= 12)
        assertTrue(meals.count { "VEGETARIAN" in it.dietTags.split(',') } >= 20)
    }

    @Test
    fun kcalComesFromMacros() {
        meals.forEach { assertEquals(it.id, Macros(it.proteinG, it.carbsG, it.fatG).kcal, it.kcal) }
    }
}
