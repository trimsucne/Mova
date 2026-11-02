package co.edu.unal.dadm.mova.presentation.viewmodel.nutrition

import co.edu.unal.dadm.mova.core.common.Result
import co.edu.unal.dadm.mova.domain.model.MealSource
import co.edu.unal.dadm.mova.domain.model.MealType
import co.edu.unal.dadm.mova.domain.usecase.LogMealUseCase
import co.edu.unal.dadm.mova.testutil.FakeMealRepository
import co.edu.unal.dadm.mova.testutil.MainDispatcherRule
import co.edu.unal.dadm.mova.testutil.TestMeals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

class MealDetailViewModelTest {
    @get:Rule val mainRule = MainDispatcherRule()

    private val today = LocalDate.of(2026, 11, 2)
    private val meal = TestMeals.meal("m1", protein = 10.0, carbs = 20.0, fat = 5.0, suitableFor = setOf(MealType.SNACK, MealType.LUNCH), servingGrams = 100)
    private val repo = FakeMealRepository(listOf(meal))

    private fun viewModel(date: LocalDate = today, mealId: String = "m1") =
        MealDetailViewModel(mealId, date, today, repo, LogMealUseCase(repo), { "u1" }, { Instant.EPOCH })

    @Test
    fun `starts with base serving and first suitable meal type`() {
        val state = viewModel().state.value
        assertEquals(100, state.grams)
        assertEquals(165, state.kcal)
        assertEquals(MealType.LUNCH, state.mealType)
    }

    @Test
    fun `changing grams scales macros`() {
        val vm = viewModel()
        vm.onEvent(MealDetailEvent.GramsChanged(150))
        assertEquals(15.0, vm.state.value.macros.proteinG, 0.001)
        assertEquals(248, vm.state.value.kcal)
    }

    @Test
    fun `add meal logs a catalog entry on the selected date`() {
        val vm = viewModel(date = today.minusDays(2))
        vm.onEvent(MealDetailEvent.MealTypeChanged(MealType.DINNER))
        vm.onEvent(MealDetailEvent.AddMeal)
        val log = repo.logs.value.single()
        assertEquals(MealSource.CATALOG, log.source)
        assertEquals(today.minusDays(2), log.date)
        assertEquals(MealType.DINNER, log.mealType)
        assertEquals(today.minusDays(2), vm.state.value.addedOn)
    }

    @Test
    fun `future dates are logged today`() {
        val vm = viewModel(date = today.plusDays(3))
        vm.onEvent(MealDetailEvent.AddMeal)
        assertEquals(today, repo.logs.value.single().date)
    }

    @Test
    fun `failed log shows error and does not navigate`() {
        repo.logResult = Result.Error("boom")
        val vm = viewModel()
        vm.onEvent(MealDetailEvent.AddMeal)
        assertEquals("boom", vm.state.value.error)
        assertNull(vm.state.value.addedOn)
    }

    @Test
    fun `unknown meal shows error`() {
        assertEquals("Meal not found", viewModel(mealId = "nope").state.value.error)
    }
}
