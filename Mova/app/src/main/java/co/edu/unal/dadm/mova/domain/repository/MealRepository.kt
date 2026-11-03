package co.edu.unal.dadm.mova.domain.repository

import co.edu.unal.dadm.mova.core.common.Result
import co.edu.unal.dadm.mova.domain.model.Meal
import co.edu.unal.dadm.mova.domain.model.MealLog
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/** Contrato congelado (D1). No cambiar firmas sin acordarlo con los módulos que lo usan. */
interface MealRepository {
    fun observeCatalog(): Flow<List<Meal>>
    fun observeMeal(id: String): Flow<Meal?>
    fun observeLogs(userId: String, date: LocalDate): Flow<List<MealLog>>
    fun observeLogs(userId: String, from: LocalDate, to: LocalDate): Flow<List<MealLog>>
    suspend fun log(entry: MealLog): Result<String>
    suspend fun delete(logId: String): Result<Unit>
}
