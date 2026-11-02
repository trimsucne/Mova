package co.edu.unal.dadm.mova.domain.usecase

import co.edu.unal.dadm.mova.domain.model.FitnessLevel
import co.edu.unal.dadm.mova.domain.model.Gender
import co.edu.unal.dadm.mova.domain.model.Goal
import co.edu.unal.dadm.mova.domain.model.Macros
import co.edu.unal.dadm.mova.domain.model.UserProfile
import co.edu.unal.dadm.mova.domain.model.defaultHeightCm
import kotlin.math.roundToInt

data class CalorieGoal(val kcal: Int, val macros: Macros)

/** Mifflin–St Jeor × nivel × objetivo + kcal quemadas (spec de Nutrición §4.5 y §13.4). */
class CalculateCalorieGoalUseCase {
    operator fun invoke(profile: UserProfile?, burnedKcal: Int = 0): CalorieGoal {
        val weight = profile?.weightKg ?: DEFAULT_WEIGHT_KG
        val age = profile?.age ?: DEFAULT_AGE
        val gender = profile?.gender
        val height = profile?.heightCm ?: defaultHeightCm(gender)
        val level = profile?.fitnessLevel ?: FitnessLevel.INTERMEDIATE
        val goal = profile?.goal ?: Goal.EXPLORE

        val bmr = 10 * weight + 6.25 * height - 5 * age + genderConstant(gender)
        val raw = bmr * activityFactor(level) * goalFactor(goal) + burnedKcal
        val kcal = maxOf(MIN_KCAL, (raw / 10).roundToInt() * 10)

        val protein = weight * if (goal == Goal.GAIN_MUSCLE) 2.0 else 1.6
        val fat = kcal * 0.25 / 9
        val carbs = maxOf(0.0, (kcal - protein * 4 - fat * 9) / 4)
        return CalorieGoal(kcal, Macros(protein, carbs, fat))
    }

    private fun genderConstant(gender: Gender?) = when (gender) {
        Gender.MALE -> 5.0
        Gender.FEMALE -> -161.0
        Gender.PREFER_NOT_TO_SAY, null -> -78.0
    }

    private fun activityFactor(level: FitnessLevel) = when (level) {
        FitnessLevel.BEGINNER -> 1.2
        FitnessLevel.NOVICE -> 1.375
        FitnessLevel.INTERMEDIATE -> 1.55
        FitnessLevel.ADVANCED -> 1.725
        FitnessLevel.ATHLETE -> 1.9
    }

    private fun goalFactor(goal: Goal) = when (goal) {
        Goal.LOSE_WEIGHT -> 0.85
        Goal.GAIN_MUSCLE -> 1.10
        Goal.IMPROVE_ENDURANCE, Goal.AI_COACH, Goal.EXPLORE -> 1.0
    }

    private companion object {
        const val DEFAULT_WEIGHT_KG = 70.0
        const val DEFAULT_AGE = 25
        const val MIN_KCAL = 1200
    }
}
