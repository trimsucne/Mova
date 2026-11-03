package co.edu.unal.dadm.mova.domain.model

import kotlin.math.roundToInt

data class Macros(
    val proteinG: Double,
    val carbsG: Double,
    val fatG: Double
) {
    val kcal: Int get() = (4 * proteinG + 4 * carbsG + 9 * fatG).roundToInt()

    operator fun plus(other: Macros) =
        Macros(proteinG + other.proteinG, carbsG + other.carbsG, fatG + other.fatG)

    fun scaled(factor: Double) = Macros(proteinG * factor, carbsG * factor, fatG * factor)

    companion object {
        val ZERO = Macros(0.0, 0.0, 0.0)
    }
}
