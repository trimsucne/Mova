package co.edu.unal.dadm.mova.domain.usecase

import co.edu.unal.dadm.mova.core.common.Result
import co.edu.unal.dadm.mova.domain.model.MealLog
import co.edu.unal.dadm.mova.domain.repository.MealRepository

// Registrar desde cualquier pantalla (catálogo, manual, Coach)
class LogMealUseCase(private val repository: MealRepository) {
    suspend operator fun invoke(entry: MealLog): Result<String> = repository.log(entry)
}
