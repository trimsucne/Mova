package co.edu.unal.dadm.mova.domain.usecase

import co.edu.unal.dadm.mova.domain.model.FitnessLevel
import co.edu.unal.dadm.mova.domain.model.Gender
import co.edu.unal.dadm.mova.domain.model.Goal
import co.edu.unal.dadm.mova.domain.model.UserProfile
import org.junit.Assert.assertEquals
import org.junit.Test

class CalculateCalorieGoalUseCaseTest {
    private val calculate = CalculateCalorieGoalUseCase()

    private fun profile(
        gender: Gender? = Gender.MALE, age: Int? = 30, weightKg: Double? = 80.0, heightCm: Int? = 180,
        level: FitnessLevel? = FitnessLevel.INTERMEDIATE, goal: Goal? = Goal.LOSE_WEIGHT
    ) = UserProfile(userId = "u", gender = gender, age = age, weightKg = weightKg, heightCm = heightCm, fitnessLevel = level, goal = goal)

    @Test
    fun `male losing weight`() {
        // BMR = 800 + 1125 - 150 + 5 = 1780; x1.55 x0.85 = 2345.15 -> 2350
        val goal = calculate(profile())
        assertEquals(2350, goal.kcal)
        assertEquals(128.0, goal.macros.proteinG, 0.01) // 1.6 g/kg
        assertEquals(65.28, goal.macros.fatG, 0.01)     // 25 % de 2350 / 9
        assertEquals(312.63, goal.macros.carbsG, 0.01)  // (2350 - 512 - 587.5) / 4
    }

    @Test
    fun `female beginner explore`() {
        // 600 + 1031.25 - 125 - 161 = 1345.25; x1.2 = 1614.3 -> 1610
        assertEquals(1610, calculate(profile(Gender.FEMALE, 25, 60.0, 165, FitnessLevel.BEGINNER, Goal.EXPLORE)).kcal)
    }

    @Test
    fun `gain muscle uses 2 g per kg and 1_10 factor`() {
        val goal = calculate(profile(goal = Goal.GAIN_MUSCLE))
        assertEquals(3030, goal.kcal) // 1780 x 1.55 x 1.10 = 3034.9 -> 3030
        assertEquals(160.0, goal.macros.proteinG, 0.01)
    }

    @Test
    fun `missing profile uses defaults`() {
        // 25 años, 70 kg, sin género -> 165 cm y -78; INTERMEDIATE, EXPLORE
        // 700 + 1031.25 - 125 - 78 = 1528.25; x1.55 = 2368.79 -> 2370
        assertEquals(2370, calculate(null).kcal)
    }

    @Test
    fun `missing height uses gender default`() {
        // MALE sin altura -> 170 cm: 800 + 1062.5 - 150 + 5 = 1717.5; x1.55 x0.85 = 2262.8 -> 2260
        assertEquals(2260, calculate(profile(heightCm = null)).kcal)
    }

    @Test
    fun `prefer not to say uses -78`() {
        // 800 + 1125 - 150 - 78 = 1697; x1.55 x0.85 = 2235.8 -> 2240
        assertEquals(2240, calculate(profile(gender = Gender.PREFER_NOT_TO_SAY)).kcal)
    }

    @Test
    fun `never below 1200`() {
        assertEquals(1200, calculate(profile(Gender.FEMALE, 70, 40.0, 150, FitnessLevel.BEGINNER, Goal.LOSE_WEIGHT)).kcal)
    }

    @Test
    fun `burned kcal raise the goal`() {
        // 2345.15 + 300 = 2645.15 -> 2650
        assertEquals(2650, calculate(profile(), burnedKcal = 300).kcal)
    }
}
