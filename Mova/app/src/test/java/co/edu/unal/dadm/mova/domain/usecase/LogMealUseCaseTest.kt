package co.edu.unal.dadm.mova.domain.usecase

import co.edu.unal.dadm.mova.core.common.Result
import co.edu.unal.dadm.mova.testutil.FakeMealRepository
import co.edu.unal.dadm.mova.testutil.TestMeals
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class LogMealUseCaseTest {
    @Test
    fun `delegates to the repository`() = runTest {
        val repo = FakeMealRepository()
        val entry = TestMeals.log("a", LocalDate.of(2026, 11, 2))
        assertEquals(Result.Success("a"), LogMealUseCase(repo)(entry))
        assertEquals(listOf(entry), repo.logs.value)
    }
}
