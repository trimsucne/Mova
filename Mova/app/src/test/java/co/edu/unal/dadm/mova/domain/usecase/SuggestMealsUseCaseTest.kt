package co.edu.unal.dadm.mova.domain.usecase

import co.edu.unal.dadm.mova.domain.model.DailyIntake
import co.edu.unal.dadm.mova.domain.model.Diet
import co.edu.unal.dadm.mova.domain.model.Macros
import co.edu.unal.dadm.mova.testutil.TestMeals
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class SuggestMealsUseCaseTest {
    private val suggest = SuggestMealsUseCase()
    private val vegan = TestMeals.meal("vegan", diets = TestMeals.VEGAN, protein = 5.0)
    private val veggie = TestMeals.meal("veggie", diets = TestMeals.VEGETARIAN, protein = 20.0)
    private val meat = TestMeals.meal("meat", diets = TestMeals.MEAT, protein = 40.0)
    private val catalog = listOf(vegan, veggie, meat)

    private fun intake(consumedProtein: Double, remainingKcal: Int) = DailyIntake(
        date = LocalDate.of(2026, 11, 2), consumed = Macros(consumedProtein, 0.0, 0.0), consumedKcal = 0,
        goalKcal = 2000, burnedKcal = 0, remainingKcal = remainingKcal, goalMacros = Macros(100.0, 200.0, 60.0), byMealType = emptyMap()
    )

    @Test
    fun `vegan only sees vegan meals`() {
        assertEquals(listOf(vegan), suggest(catalog, Diet.VEGAN, null))
    }

    @Test
    fun `vegetarian sees vegan and vegetarian`() {
        assertEquals(setOf(vegan, veggie), suggest(catalog, Diet.VEGETARIAN, null).toSet())
    }

    @Test
    fun `null diet and carnivore see everything`() {
        assertEquals(3, suggest(catalog, null, null).size)
        assertEquals(3, suggest(catalog, Diet.CARNIVORE, null).size)
    }

    @Test
    fun `protein first when more than 30 percent is missing`() {
        // faltan 60 g de 100 g
        assertEquals(listOf(meat, veggie, vegan), suggest(catalog, null, intake(consumedProtein = 40.0, remainingKcal = 500)))
    }

    @Test
    fun `otherwise closest kcal to what is left`() {
        // faltan 10 g de 100 g; kcal: vegan 105, veggie 165, meat 245
        assertEquals(listOf(veggie, meat, vegan), suggest(catalog, null, intake(consumedProtein = 90.0, remainingKcal = 180)))
    }

    @Test
    fun `at most five`() {
        val many = (1..8).map { TestMeals.meal("m$it") }
        assertEquals(5, suggest(many, null, null).size)
    }
}
