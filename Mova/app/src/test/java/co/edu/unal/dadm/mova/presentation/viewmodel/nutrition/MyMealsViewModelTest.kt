package co.edu.unal.dadm.mova.presentation.viewmodel.nutrition

import co.edu.unal.dadm.mova.domain.model.MealType
import co.edu.unal.dadm.mova.testutil.FakeMealRepository
import co.edu.unal.dadm.mova.testutil.MainDispatcherRule
import co.edu.unal.dadm.mova.testutil.TestMeals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

class MyMealsViewModelTest {
    @get:Rule val mainRule = MainDispatcherRule()

    private val today = LocalDate.of(2026, 11, 2)
    private val repo = FakeMealRepository().apply {
        logs.value = listOf(
            TestMeals.log("b", today, mealType = MealType.BREAKFAST),
            TestMeals.log("l", today, mealType = MealType.LUNCH),
            TestMeals.log("y", today.minusDays(1), mealType = MealType.DINNER)
        )
    }

    private fun viewModel(initial: LocalDate = today) = MyMealsViewModel(initial, today, repo) { "u1" }

    @Test
    fun `seven days ending today`() {
        val days = viewModel().state.value.days
        assertEquals(7, days.size)
        assertEquals(today.minusDays(6), days.first())
        assertEquals(today, days.last())
    }

    @Test
    fun `shows logs of the selected day`() {
        val vm = viewModel()
        assertEquals(setOf("b", "l"), vm.state.value.visibleLogs.map { it.id }.toSet())
        vm.onEvent(MyMealsEvent.SelectDate(today.minusDays(1)))
        assertEquals(listOf("y"), vm.state.value.visibleLogs.map { it.id })
    }

    @Test
    fun `filters by meal type`() {
        val vm = viewModel()
        vm.onEvent(MyMealsEvent.SelectFilter(MealType.LUNCH))
        assertEquals(listOf("l"), vm.state.value.visibleLogs.map { it.id })
        vm.onEvent(MyMealsEvent.SelectFilter(null))
        assertEquals(2, vm.state.value.visibleLogs.size)
    }

    @Test
    fun `delete asks for confirmation first`() {
        val vm = viewModel()
        val target = vm.state.value.visibleLogs.first { it.id == "b" }
        vm.onEvent(MyMealsEvent.RequestDelete(target))
        assertEquals(3, repo.logs.value.size)
        vm.onEvent(MyMealsEvent.ConfirmDelete)
        assertEquals(2, repo.logs.value.size)
        assertNull(vm.state.value.pendingDelete)
    }

    @Test
    fun `cancel keeps the log`() {
        val vm = viewModel()
        vm.onEvent(MyMealsEvent.RequestDelete(vm.state.value.visibleLogs.first()))
        vm.onEvent(MyMealsEvent.CancelDelete)
        assertEquals(3, repo.logs.value.size)
    }
}
