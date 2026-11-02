package co.edu.unal.dadm.mova.presentation.viewmodel.home

import co.edu.unal.dadm.mova.domain.model.MealSource
import co.edu.unal.dadm.mova.domain.model.MealType
import co.edu.unal.dadm.mova.domain.model.SearchResult
import co.edu.unal.dadm.mova.domain.model.Searchable
import co.edu.unal.dadm.mova.domain.usecase.LogMealUseCase
import co.edu.unal.dadm.mova.domain.usecase.SearchCatalogUseCase
import co.edu.unal.dadm.mova.testutil.FakeMealRepository
import co.edu.unal.dadm.mova.testutil.MainDispatcherRule
import co.edu.unal.dadm.mova.testutil.TestMeals
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

class SearchViewModelTest {
    @get:Rule val mainRule = MainDispatcherRule(StandardTestDispatcher())

    private val today = LocalDate.of(2026, 11, 2)
    private fun meal(id: String, title: String) = Searchable(id, SearchResult.Type.MEAL, title, "", id, "Grains", emptyList())
    private val search = FakeSearchRepository(
        listOf(meal("toast", "Avocado Toast"), meal("oats", "Oatmeal"), meal("salad", "Quinoa Salad"), meal("tuna", "Tuna Sandwich"))
    )
    private val recents = FakeRecentSearches()
    private val meals = FakeMealRepository(listOf(TestMeals.meal("toast", name = "Avocado Toast", suitableFor = setOf(MealType.SNACK, MealType.BREAKFAST), servingGrams = 150)))

    private fun viewModel(initialQuery: String = "", workouts: Boolean = false) = SearchViewModel(
        initialQuery, workouts, SearchCatalogUseCase(search), search, search, recents,
        meals, LogMealUseCase(meals), { "u1" }, { today }, { Instant.EPOCH }
    )

    @Test
    fun `waits 300 ms after the last key`() = runTest(mainRule.dispatcher) {
        val vm = viewModel()
        vm.onEvent(SearchEvent.QueryChanged("toa"))
        advanceTimeBy(299)
        runCurrent()
        assertEquals(SearchStatus.LOADING, vm.state.value.status)
        assertTrue(search.queries.isEmpty())
        advanceTimeBy(2)
        runCurrent()
        assertEquals(SearchStatus.RESULTS, vm.state.value.status)
        assertEquals(listOf("Avocado Toast"), vm.state.value.results.map { it.title })
    }

    @Test
    fun `fewer than two characters does not search`() = runTest(mainRule.dispatcher) {
        val vm = viewModel()
        vm.onEvent(SearchEvent.QueryChanged("t"))
        advanceUntilIdle()
        assertEquals(SearchStatus.IDLE, vm.state.value.status)
        assertTrue(search.queries.isEmpty())
    }

    @Test
    fun `only the last query of a burst is searched`() = runTest(mainRule.dispatcher) {
        val vm = viewModel()
        listOf("to", "toa", "toas").forEach {
            vm.onEvent(SearchEvent.QueryChanged(it))
            advanceTimeBy(100)
        }
        advanceUntilIdle()
        assertEquals(listOf("toas"), search.queries.map { it.first })
    }

    @Test
    fun `filters without workouts are All and Meals`() = runTest(mainRule.dispatcher) {
        assertEquals(listOf(SearchFilter.ALL, SearchFilter.MEALS), viewModel().state.value.availableFilters)
        assertEquals(SearchFilter.entries, viewModel(workouts = true).state.value.availableFilters)
    }

    @Test
    fun `filter changes the searched types`() = runTest(mainRule.dispatcher) {
        val vm = viewModel()
        vm.onEvent(SearchEvent.FilterChanged(SearchFilter.MEALS))
        vm.onEvent(SearchEvent.QueryChanged("toast"))
        advanceUntilIdle()
        assertEquals(setOf(SearchResult.Type.MEAL), search.queries.last().second)
    }

    @Test
    fun `no results shows empty state with three fallbacks`() = runTest(mainRule.dispatcher) {
        val vm = viewModel()
        vm.onEvent(SearchEvent.QueryChanged("pizza"))
        advanceUntilIdle()
        assertEquals(SearchStatus.EMPTY, vm.state.value.status)
        assertEquals(listOf("Avocado Toast", "Oatmeal", "Quinoa Salad"), vm.state.value.fallback.map { it.title })
    }

    @Test
    fun `error state and retry`() = runTest(mainRule.dispatcher) {
        search.failing = true
        val vm = viewModel()
        vm.onEvent(SearchEvent.QueryChanged("toast"))
        advanceUntilIdle()
        assertEquals(SearchStatus.ERROR, vm.state.value.status)
        search.failing = false
        vm.onEvent(SearchEvent.Retry)
        advanceUntilIdle()
        assertEquals(SearchStatus.RESULTS, vm.state.value.status)
    }

    @Test
    fun `initial query from the route is searched`() = runTest(mainRule.dispatcher) {
        val vm = viewModel(initialQuery = "oat")
        advanceUntilIdle()
        assertEquals(listOf("Oatmeal"), vm.state.value.results.map { it.title })
    }

    @Test
    fun `suggestions while typing`() = runTest(mainRule.dispatcher) {
        val vm = viewModel()
        vm.onEvent(SearchEvent.QueryChanged("qu"))
        advanceUntilIdle()
        assertEquals(listOf("Quinoa Salad"), vm.state.value.suggestions)
        vm.onEvent(SearchEvent.SuggestionClicked("Quinoa Salad"))
        advanceUntilIdle()
        assertEquals("Quinoa Salad", vm.state.value.query)
        assertEquals(100, vm.state.value.results.first().score)
    }

    @Test
    fun `recents are saved on submit and on open, and can be cleared`() = runTest(mainRule.dispatcher) {
        val vm = viewModel()
        vm.onEvent(SearchEvent.QueryChanged("toast"))
        vm.onEvent(SearchEvent.Submit)
        advanceUntilIdle()
        vm.onEvent(SearchEvent.QueryChanged("oat"))
        advanceUntilIdle()
        vm.onEvent(SearchEvent.ResultOpened(vm.state.value.results.first()))
        advanceUntilIdle()
        assertEquals(listOf("oat", "toast"), vm.state.value.recent)
        vm.onEvent(SearchEvent.ClearRecent)
        advanceUntilIdle()
        assertTrue(vm.state.value.recent.isEmpty())
    }

    @Test
    fun `add from result logs base serving today and undo deletes it`() = runTest(mainRule.dispatcher) {
        val vm = viewModel()
        vm.onEvent(SearchEvent.QueryChanged("toast"))
        advanceUntilIdle()
        vm.onEvent(SearchEvent.AddMeal(vm.state.value.results.first()))
        advanceUntilIdle()
        val log = meals.logs.value.single()
        assertEquals(150, log.grams)
        assertEquals(today, log.date)
        assertEquals(MealType.BREAKFAST, log.mealType) // primero en el orden BREAKFAST, LUNCH, DINNER, SNACK
        assertEquals(MealSource.CATALOG, log.source)
        assertEquals(MealType.BREAKFAST, vm.state.value.added!!.mealType)

        vm.onEvent(SearchEvent.UndoAdd)
        advanceUntilIdle()
        assertTrue(meals.logs.value.isEmpty())
        assertNull(vm.state.value.added)
    }
}
