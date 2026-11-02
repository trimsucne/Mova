package co.edu.unal.dadm.mova.domain.usecase

import co.edu.unal.dadm.mova.domain.model.DailyIntake
import co.edu.unal.dadm.mova.domain.model.Macros
import co.edu.unal.dadm.mova.domain.repository.BurnedKcalProvider
import co.edu.unal.dadm.mova.domain.repository.MealRepository
import co.edu.unal.dadm.mova.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import java.time.LocalDate

// Ingesta, meta y restante de un día
class ObserveDailyIntakeUseCase(
    private val mealRepository: MealRepository,
    private val profileRepository: ProfileRepository,
    private val burnedKcal: BurnedKcalProvider,
    private val calculateGoal: CalculateCalorieGoalUseCase,
    private val currentUserId: () -> String
) {
    operator fun invoke(date: LocalDate): Flow<DailyIntake> {
        val userId = currentUserId()
        // Sin perfil (sin sesión o error de red) se usan los valores por defecto de la meta.
        val profile = profileRepository.observe(userId).catch { emit(null) }
        return combine(
            mealRepository.observeLogs(userId, date),
            profile,
            burnedKcal.observeBurnedKcal(userId, date)
        ) { logs, userProfile, burned ->
            val consumed = logs.fold(Macros.ZERO) { total, log -> total + log.macros }
            val consumedKcal = logs.sumOf { it.kcal }
            val goal = calculateGoal(userProfile, burned)
            DailyIntake(
                date = date,
                consumed = consumed,
                consumedKcal = consumedKcal,
                goalKcal = goal.kcal,
                burnedKcal = burned,
                remainingKcal = goal.kcal - consumedKcal,
                goalMacros = goal.macros,
                byMealType = logs.groupBy { it.mealType }
            )
        }
    }
}
