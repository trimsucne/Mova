package co.edu.unal.dadm.mova.domain.usecase

import co.edu.unal.dadm.mova.domain.model.Macros

// kcal = 4P + 4C + 9G
class CalculateKcalFromMacrosUseCase {
    operator fun invoke(macros: Macros): Int = macros.kcal
}
