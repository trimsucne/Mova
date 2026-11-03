package co.edu.unal.dadm.mova.domain.usecase

import co.edu.unal.dadm.mova.domain.model.FitnessLevel
import co.edu.unal.dadm.mova.domain.model.Gender
import co.edu.unal.dadm.mova.domain.model.Goal
import co.edu.unal.dadm.mova.domain.model.MealType
import co.edu.unal.dadm.mova.domain.model.UserProfile
import co.edu.unal.dadm.mova.domain.repository.BurnedKcalProvider
import co.edu.unal.dadm.mova.testutil.FakeMealRepository
import co.edu.unal.dadm.mova.testutil.FakeProfileRepository
import co.edu.unal.dadm.mova.testutil.TestMeals
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

// onStart emite primero con perfil null (meta por defecto); las aserciones usan la emisión con el perfil ya recibido.
class ObserveDailyIntakeUseCaseTest {
    private val date = LocalDate.of(2026, 11, 2)
    private val profile = UserProfile(
        userId = "u1", gender = Gender.MALE, age = 30, weightKg = 80.0, heightCm = 180,
        fitnessLevel = FitnessLevel.INTERMEDIATE, goal = Goal.LOSE_WEIGHT
    )
    private val meals = FakeMealRepository().apply {
        logs.value = listOf(
            TestMeals.log("a", date, protein = 30.0, carbs = 50.0, fat = 10.0, mealType = MealType.BREAKFAST), // 410
            TestMeals.log("b", date, protein = 20.0, carbs = 20.0, fat = 10.0, mealType = MealType.LUNCH),     // 250
            TestMeals.log("c", date.minusDays(1))
        )
    }

    private fun useCase(burned: BurnedKcalProvider = BurnedKcalProvider.NONE) =
        ObserveDailyIntakeUseCase(meals, FakeProfileRepository(profile), burned, CalculateCalorieGoalUseCase()) { "u1" }

    @Test
    fun `sums the day and subtracts from the goal`() = runTest {
        val intake = useCase()(date).drop(1).first()
        assertEquals(660, intake.consumedKcal)
        assertEquals(2350, intake.goalKcal)
        assertEquals(1690, intake.remainingKcal)
        assertEquals(50.0, intake.consumed.proteinG, 0.001)
        assertEquals(setOf(MealType.BREAKFAST, MealType.LUNCH), intake.byMealType.keys)
    }

    @Test
    fun `burned kcal raise the goal`() = runTest {
        val intake = useCase { _, _ -> flowOf(300) }(date).drop(1).first()
        assertEquals(300, intake.burnedKcal)
        assertEquals(2650, intake.goalKcal)
    }

    @Test
    fun `remaining can be negative`() = runTest {
        meals.logs.value = listOf(TestMeals.log("big", date, protein = 200.0, carbs = 400.0, fat = 100.0)) // 3300
        assertEquals(-950, useCase()(date).drop(1).first().remainingKcal)
    }
}
