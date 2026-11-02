package co.edu.unal.dadm.mova.presentation.viewmodel.nutrition

import co.edu.unal.dadm.mova.domain.model.MealSource
import co.edu.unal.dadm.mova.domain.model.MealType
import co.edu.unal.dadm.mova.domain.usecase.CalculateKcalFromMacrosUseCase
import co.edu.unal.dadm.mova.domain.usecase.LogMealUseCase
import co.edu.unal.dadm.mova.testutil.FakeMealRepository
import co.edu.unal.dadm.mova.testutil.MainDispatcherRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

class AddMealViewModelTest {
    @get:Rule val mainRule = MainDispatcherRule()

    private val today = LocalDate.of(2026, 11, 2)
    private val repo = FakeMealRepository()
    private val vm = AddMealViewModel(today, today, MealType.LUNCH, LogMealUseCase(repo), CalculateKcalFromMacrosUseCase(), { "u1" }, { Instant.EPOCH })

    private fun fill(name: String, p: String, c: String, f: String) {
        vm.onEvent(AddMealEvent.NameChanged(name))
        vm.onEvent(AddMealEvent.ProteinChanged(p))
        vm.onEvent(AddMealEvent.CarbsChanged(c))
        vm.onEvent(AddMealEvent.FatChanged(f))
    }

    @Test
    fun `kcal update while typing`() {
        fill("Arepa", "5", "30", "8")
        assertEquals(212, vm.state.value.kcal)
    }

    @Test
    fun `initial meal type comes from the route`() {
        assertEquals(MealType.LUNCH, vm.state.value.mealType)
    }

    @Test
    fun `short name is rejected`() {
        fill("A", "5", "30", "8")
        vm.onEvent(AddMealEvent.Save)
        assertNotNull(vm.state.value.nameError)
        assertTrue(repo.logs.value.isEmpty())
    }

    @Test
    fun `macro over 300 is rejected`() {
        fill("Arepa", "301", "0", "0")
        vm.onEvent(AddMealEvent.Save)
        assertNotNull(vm.state.value.macroError)
    }

    @Test
    fun `all macros zero is rejected`() {
        fill("Arepa", "", "0", "")
        vm.onEvent(AddMealEvent.Save)
        assertNotNull(vm.state.value.macroError)
    }

    @Test
    fun `valid form saves a manual log`() {
        fill("  Arepa  ", "5", "30,5", "8")
        vm.onEvent(AddMealEvent.MealTypeChanged(MealType.BREAKFAST))
        vm.onEvent(AddMealEvent.Save)
        val log = repo.logs.value.single()
        assertEquals("Arepa", log.name)
        assertEquals(30.5, log.macros.carbsG, 0.001)
        assertEquals(MealSource.MANUAL, log.source)
        assertEquals(MealType.BREAKFAST, log.mealType)
        assertEquals(today, vm.state.value.addedOn)
    }
}
