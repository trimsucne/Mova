package co.edu.unal.dadm.mova.data.repository

import co.edu.unal.dadm.mova.domain.model.MealCategory
import co.edu.unal.dadm.mova.domain.model.SearchResult
import co.edu.unal.dadm.mova.domain.model.Searchable
import co.edu.unal.dadm.mova.domain.repository.WorkoutSearchSource
import co.edu.unal.dadm.mova.testutil.FakeMealRepository
import co.edu.unal.dadm.mova.testutil.TestMeals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchRepositoryImplTest {
    private val meals = FakeMealRepository(
        listOf(
            TestMeals.meal("toast", name = "Avocado Toast", category = MealCategory.GRAINS),
            TestMeals.meal("salad", name = "Quinoa Salad", category = MealCategory.SALAD_VEGETABLES),
            TestMeals.meal("pina", name = "Piña Smoothie", category = MealCategory.DRINK),
            TestMeals.meal("tuna", name = "Tuna Sandwich", category = MealCategory.PROTEIN)
        )
    )
    private val workouts = object : WorkoutSearchSource {
        override val isAvailable = true
        override fun observe() = flowOf(listOf(Searchable("w1", SearchResult.Type.WORKOUT, "Toast Burner HIIT", "20 min · HIIT", null, "HIIT", emptyList())))
    }

    private fun TestScope.repo(source: WorkoutSearchSource = WorkoutSearchSource.NONE) =
        SearchRepositoryImpl(meals, source, UnconfinedTestDispatcher(testScheduler))

    @Test
    fun `meal results carry kcal and category subtitle`() = runTest {
        val result = repo().search("toast", setOf(SearchResult.Type.MEAL)).first().single()
        assertEquals("Avocado Toast", result.title)
        assertEquals("${TestMeals.meal("x").kcal} kcal · Grains", result.subtitle)
        assertEquals(70, result.score)
        assertEquals("toast", result.imageRes)
    }

    @Test
    fun `type filter is applied`() = runTest {
        val all = repo(workouts).search("toast", setOf(SearchResult.Type.MEAL, SearchResult.Type.WORKOUT)).first()
        assertEquals(2, all.size)
        val onlyWorkouts = repo(workouts).search("toast", setOf(SearchResult.Type.WORKOUT)).first()
        assertEquals(listOf("w1"), onlyWorkouts.map { it.id })
    }

    @Test
    fun `accents are ignored`() = runTest {
        assertEquals(listOf("pina"), repo().search("pina", setOf(SearchResult.Type.MEAL)).first().map { it.id })
    }

    @Test
    fun `suggestions are name prefixes, alphabetical, at most six`() = runTest {
        assertEquals(listOf("Quinoa Salad"), repo().suggestions("qui").first())
        assertTrue(repo().suggestions("zzz").first().isEmpty())
        val many = FakeMealRepository((1..9).map { TestMeals.meal("t$it", name = "Toast $it") })
        val names = SearchRepositoryImpl(many, WorkoutSearchSource.NONE, UnconfinedTestDispatcher(testScheduler)).suggestions("toast").first()
        assertEquals(listOf("Toast 1", "Toast 2", "Toast 3", "Toast 4", "Toast 5", "Toast 6"), names)
    }

    @Test
    fun `first items are alphabetical within the filter`() = runTest {
        val fallback = repo().firstItems(setOf(SearchResult.Type.MEAL), 3).first()
        assertEquals(listOf("Avocado Toast", "Piña Smoothie", "Quinoa Salad"), fallback.map { it.title })
    }
}
