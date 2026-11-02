package co.edu.unal.dadm.mova.presentation.viewmodel.nutrition

import co.edu.unal.dadm.mova.domain.model.Macros
import co.edu.unal.dadm.mova.testutil.FakeMealRepository
import co.edu.unal.dadm.mova.testutil.MainDispatcherRule
import co.edu.unal.dadm.mova.testutil.TestMeals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

class CalorieStatsViewModelTest {
    @get:Rule val mainRule = MainDispatcherRule()

    @Test
    fun `share always sums 100`() {
        // kcal: P 100, C 200, G 90 -> 25.6 %, 51.3 %, 23.1 %
        val share = CalorieStatsViewModel.share(Macros(25.0, 50.0, 10.0))!!
        assertEquals(26, share.proteinPct)
        assertEquals(51, share.carbsPct)
        assertEquals(23, share.fatPct)
        assertEquals(100, share.proteinPct + share.carbsPct + share.fatPct)
    }

    @Test
    fun `zero fat never goes negative`() {
        // kcal: P 404, C 396, G 0 -> 50.5 %, 49.5 %, 0 %
        val share = CalorieStatsViewModel.share(Macros(101.0, 99.0, 0.0))!!
        assertTrue(share.proteinPct >= 0)
        assertTrue(share.carbsPct >= 0)
        assertTrue(share.fatPct >= 0)
        assertEquals(0, share.fatPct)
        assertEquals(100, share.proteinPct + share.carbsPct + share.fatPct)
    }

    @Test
    fun `no data gives no share`() {
        assertNull(CalorieStatsViewModel.share(Macros.ZERO))
    }

    @Test
    fun `uses only the current month`() {
        val today = LocalDate.of(2026, 11, 15)
        val repo = FakeMealRepository().apply {
            logs.value = listOf(
                TestMeals.log("in", LocalDate.of(2026, 11, 1), protein = 25.0, carbs = 50.0, fat = 10.0),
                TestMeals.log("out", LocalDate.of(2026, 10, 31), protein = 100.0, carbs = 0.0, fat = 0.0)
            )
        }
        val state = CalorieStatsViewModel(today, repo) { "u1" }.state.value
        assertEquals(390, state.totalKcal)
        assertEquals(26, state.share!!.proteinPct)
    }

    @Test
    fun `exposes month grams per macro`() {
        val today = LocalDate.of(2026, 11, 15)
        val repo = FakeMealRepository().apply {
            logs.value = listOf(
                TestMeals.log("a", LocalDate.of(2026, 11, 1), protein = 25.0, carbs = 50.0, fat = 10.0),
                TestMeals.log("b", LocalDate.of(2026, 11, 2), protein = 5.0, carbs = 10.0, fat = 2.0),
                TestMeals.log("out", LocalDate.of(2026, 10, 31), protein = 100.0, carbs = 0.0, fat = 0.0)
            )
        }
        val macros = CalorieStatsViewModel(today, repo) { "u1" }.state.value.monthMacros
        assertEquals(30.0, macros.proteinG, 0.001)
        assertEquals(60.0, macros.carbsG, 0.001)
        assertEquals(12.0, macros.fatG, 0.001)
    }
}
