package co.edu.unal.dadm.mova.testutil

import co.edu.unal.dadm.mova.core.common.Result
import co.edu.unal.dadm.mova.domain.model.Meal
import co.edu.unal.dadm.mova.domain.model.MealLog
import co.edu.unal.dadm.mova.domain.repository.MealRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class FakeMealRepository(catalog: List<Meal> = emptyList()) : MealRepository {
    val catalog = MutableStateFlow(catalog)
    val logs = MutableStateFlow<List<MealLog>>(emptyList())
    /** Si no es null, log() devuelve este resultado sin guardar nada. */
    var logResult: Result<String>? = null

    override fun observeCatalog(): Flow<List<Meal>> = catalog
    override fun observeMeal(id: String): Flow<Meal?> = catalog.map { all -> all.firstOrNull { it.id == id } }
    override fun observeLogs(userId: String, date: LocalDate): Flow<List<MealLog>> =
        logs.map { all -> all.filter { it.userId == userId && it.date == date } }
    override fun observeLogs(userId: String, from: LocalDate, to: LocalDate): Flow<List<MealLog>> =
        logs.map { all -> all.filter { it.userId == userId && !it.date.isBefore(from) && !it.date.isAfter(to) } }

    override suspend fun log(entry: MealLog): Result<String> {
        logResult?.let { return it }
        logs.value = logs.value + entry
        return Result.Success(entry.id)
    }

    override suspend fun delete(logId: String): Result<Unit> {
        logs.value = logs.value.filterNot { it.id == logId }
        return Result.Success(Unit)
    }
}
