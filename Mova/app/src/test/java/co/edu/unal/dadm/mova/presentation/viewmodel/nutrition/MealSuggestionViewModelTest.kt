package co.edu.unal.dadm.mova.presentation.viewmodel.nutrition

import co.edu.unal.dadm.mova.domain.model.Diet
import co.edu.unal.dadm.mova.domain.model.MealCategory
import co.edu.unal.dadm.mova.domain.model.UserProfile
import co.edu.unal.dadm.mova.domain.repository.BurnedKcalProvider
import co.edu.unal.dadm.mova.domain.usecase.CalculateCalorieGoalUseCase
import co.edu.unal.dadm.mova.domain.usecase.ObserveDailyIntakeUseCase
import co.edu.unal.dadm.mova.domain.usecase.SuggestMealsUseCase
import co.edu.unal.dadm.mova.testutil.FakeMealRepository
import co.edu.unal.dadm.mova.testutil.FakeProfileRepository
import co.edu.unal.dadm.mova.testutil.MainDispatcherRule
import co.edu.unal.dadm.mova.testutil.TestMeals
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

class MealSuggestionViewModelTest {
    @get:Rule val mainRule = MainDispatcherRule()

    private val salad = TestMeals.meal("salad", diets = TestMeals.VEGAN, category = MealCategory.SALAD_VEGETABLES)
    private val steak = TestMeals.meal("steak", diets = TestMeals.MEAT, category = MealCategory.PROTEIN)
    private val repo = FakeMealRepository(listOf(salad, steak))

    private fun viewModel(diet: Diet?): MealSuggestionViewModel {
        val profiles = FakeProfileRepository(UserProfile(userId = "u1", diet = diet))
        val intake = ObserveDailyIntakeUseCase(repo, profiles, BurnedKcalProvider.NONE, CalculateCalorieGoalUseCase()) { "u1" }
        return MealSuggestionViewModel(LocalDate.of(2026, 11, 2), repo, profiles, intake, SuggestMealsUseCase()) { "u1" }
    }

    @Test
    fun `catalog shows every meal and filters by category`() {
        val vm = viewModel(Diet.VEGAN)
        assertEquals(2, vm.state.value.visibleCatalog.size)
        vm.onEvent(MealSuggestionEvent.SelectCategory(MealCategory.PROTEIN))
        assertEquals(listOf(steak), vm.state.value.visibleCatalog)
    }

    @Test
    fun `suggestions respect the diet`() {
        assertEquals(listOf(salad), viewModel(Diet.VEGAN).state.value.suggestions)
        assertEquals(2, viewModel(null).state.value.suggestions.size)
    }
}
