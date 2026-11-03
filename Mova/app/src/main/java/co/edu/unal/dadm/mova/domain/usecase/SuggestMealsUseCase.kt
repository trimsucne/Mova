package co.edu.unal.dadm.mova.domain.usecase

import co.edu.unal.dadm.mova.domain.model.DailyIntake
import co.edu.unal.dadm.mova.domain.model.Diet
import co.edu.unal.dadm.mova.domain.model.Meal
import kotlin.math.abs

/**
 * Sugerencias (spec §3): se descartan comidas incompatibles con la dieta (null = sin restricción)
 * y se ordenan por proteína si falta más del 30 % de su meta; si no, por cercanía a las kcal restantes.
 */
class SuggestMealsUseCase {
    operator fun invoke(catalog: List<Meal>, diet: Diet?, intake: DailyIntake?, limit: Int = 5): List<Meal> {
        val allowed = if (diet == null) catalog else catalog.filter { diet in it.dietTags }
        if (intake == null) return allowed.take(limit)
        val proteinGoal = intake.goalMacros.proteinG
        val missingProtein = proteinGoal - intake.consumed.proteinG
        val sorted = if (missingProtein > proteinGoal * 0.3) {
            allowed.sortedByDescending { it.macros.proteinG }
        } else {
            val remaining = intake.remainingKcal.coerceAtLeast(0)
            allowed.sortedBy { abs(it.kcal - remaining) }
        }
        return sorted.take(limit)
    }
}
