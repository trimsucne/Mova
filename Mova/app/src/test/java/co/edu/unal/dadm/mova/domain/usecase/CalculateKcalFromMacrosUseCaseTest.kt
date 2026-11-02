package co.edu.unal.dadm.mova.domain.usecase

import co.edu.unal.dadm.mova.domain.model.Macros
import org.junit.Assert.assertEquals
import org.junit.Test

class CalculateKcalFromMacrosUseCaseTest {
    private val calculate = CalculateKcalFromMacrosUseCase()

    @Test
    fun `uses 4 4 9 formula`() {
        assertEquals(514, calculate(Macros(proteinG = 12.0, carbsG = 22.0, fatG = 42.0)))
    }

    @Test
    fun `rounds to nearest integer`() {
        // 4*22 + 4*1.8 + 9*19 = 266.2 (macros del mockup Avocado Dish)
        assertEquals(266, calculate(Macros(22.0, 1.8, 19.0)))
    }

    @Test
    fun `zero macros give zero kcal`() {
        assertEquals(0, calculate(Macros.ZERO))
    }
}
