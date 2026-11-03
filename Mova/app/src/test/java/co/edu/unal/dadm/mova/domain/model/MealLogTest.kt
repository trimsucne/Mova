package co.edu.unal.dadm.mova.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

class MealLogTest {
    private val meal = Meal(
        id = "meal_x", name = "X", category = MealCategory.PROTEIN,
        suitableFor = setOf(MealType.LUNCH), dietTags = setOf(Diet.CARNIVORE),
        servingGrams = 100, macros = Macros(10.0, 20.0, 5.0), kcal = 165,
        prepMinutes = 10, imageRes = "meal_x", tags = emptyList()
    )
    private val now = Instant.parse("2026-11-02T12:00:00Z")
    private val date = LocalDate.of(2026, 11, 2)

    @Test
    fun `fromMeal scales macros by grams and copies name`() {
        val log = MealLog.fromMeal(meal, grams = 150, userId = "u1", date = date, mealType = MealType.LUNCH, now = now, id = "id1")
        assertEquals(Macros(15.0, 30.0, 7.5), log.macros)
        assertEquals(248, log.kcal) // 60 + 120 + 67.5 = 247.5 -> 248
        assertEquals("X", log.name)
        assertEquals("meal_x", log.mealId)
        assertEquals(150, log.grams)
        assertEquals(MealSource.CATALOG, log.source)
    }

    @Test
    fun `manual has no meal id nor grams and trims name`() {
        val log = MealLog.manual("  Arepa  ", Macros(5.0, 30.0, 8.0), "u1", date, MealType.BREAKFAST, now, id = "id2")
        assertEquals("Arepa", log.name)
        assertNull(log.mealId)
        assertNull(log.grams)
        assertEquals(212, log.kcal)
        assertEquals(MealSource.MANUAL, log.source)
    }
}
