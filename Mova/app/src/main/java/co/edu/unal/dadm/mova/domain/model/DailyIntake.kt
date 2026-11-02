package co.edu.unal.dadm.mova.domain.model

import java.time.LocalDate

data class DailyIntake(
    val date: LocalDate,
    val consumed: Macros,
    val consumedKcal: Int,
    val goalKcal: Int,
    val burnedKcal: Int,
    val remainingKcal: Int,
    val goalMacros: Macros,
    val byMealType: Map<MealType, List<MealLog>>
)
