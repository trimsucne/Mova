package co.edu.unal.dadm.mova.presentation.viewmodel.nutrition

import co.edu.unal.dadm.mova.domain.repository.BurnedKcalProvider
import co.edu.unal.dadm.mova.domain.usecase.CalculateCalorieGoalUseCase
import co.edu.unal.dadm.mova.domain.usecase.ObserveDailyIntakeUseCase
import co.edu.unal.dadm.mova.testutil.FakeMealRepository
import co.edu.unal.dadm.mova.testutil.FakeProfileRepository
import co.edu.unal.dadm.mova.testutil.MainDispatcherRule
import co.edu.unal.dadm.mova.testutil.TestMeals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

class CalorieIntakeViewModelTest {
    @get:Rule val mainRule = MainDispatcherRule()

    private val today = LocalDate.of(2026, 11, 3)
    private val repo = FakeMealRepository()

    private fun viewModel() = CalorieIntakeViewModel(
        today,
        ObserveDailyIntakeUseCase(repo, FakeProfileRepository(null), BurnedKcalProvider.NONE, CalculateCalorieGoalUseCase()) { "u1" },
        repo
    ) { "u1" }

    @Test
    fun `monthly series has one value per day until today`() {
        repo.logs.value = listOf(
            TestMeals.log("a", LocalDate.of(2026, 11, 1)), // 125 kcal
            TestMeals.log("b", LocalDate.of(2026, 11, 1)), // 125 kcal
            TestMeals.log("c", today),                      // 125 kcal
            TestMeals.log("old", LocalDate.of(2026, 10, 31))
        )
        assertEquals(listOf(250, 0, 125), viewModel().state.value.monthlyKcal)
    }

    @Test
    fun `exceeded is null under the goal`() {
        repo.logs.value = listOf(TestMeals.log("a", today))
        assertNull(viewModel().state.value.exceededBy)
    }

    @Test
    fun `exceeded shows how much over the goal`() {
        // Meta por defecto sin perfil: 2370 kcal. 3300 consumidas -> 930 de más.
        repo.logs.value = listOf(TestMeals.log("big", today, protein = 200.0, carbs = 400.0, fat = 100.0))
        assertEquals(930, viewModel().state.value.exceededBy)
    }
}
