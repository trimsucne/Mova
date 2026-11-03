package co.edu.unal.dadm.mova.domain.usecase

import co.edu.unal.dadm.mova.data.repository.InMemoryActivityRepository
import co.edu.unal.dadm.mova.data.repository.InMemoryWorkoutRepository
import co.edu.unal.dadm.mova.domain.model.ActivityType
import co.edu.unal.dadm.mova.domain.model.WorkoutCategory
import co.edu.unal.dadm.mova.domain.model.WorkoutSeed
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FitnessUseCasesTest {
    private val back = WorkoutSeed.workouts.first { it.id == "back" }

    @Test
    fun `observeWorkouts filtra por categoria`() = runTest {
        val repo = InMemoryWorkoutRepository()
        val strength = ObserveWorkoutsUseCase(repo)(WorkoutCategory.STRENGTH).first()
        assertTrue(strength.isNotEmpty())
        assertTrue(strength.all { it.category == WorkoutCategory.STRENGTH })
    }

    @Test
    fun `completar sesion guarda y calcula kcal proporcionales`() = runTest {
        val repo = InMemoryWorkoutRepository()
        val half = back.totalSets / 2
        val log = CompleteWorkoutSessionUseCase(repo)(back, startedAt = 0, durationSec = 3000, completedSets = half)
        assertEquals(back.kcal * half / back.totalSets, log.kcalBurned)
        assertEquals(50, log.minutes)
        assertEquals(log, repo.observeSession(log.id).first())
    }

    @Test
    fun `completar todas las series da las kcal del workout`() = runTest {
        val repo = InMemoryWorkoutRepository()
        val log = CompleteWorkoutSessionUseCase(repo)(back, 0, 60, back.totalSets)
        assertEquals(back.kcal, log.kcalBurned)
    }

    @Test
    fun `logActivity calcula kcal con MET x peso x horas`() = runTest {
        val repo = InMemoryActivityRepository()
        val log = LogActivityUseCase(repo)(ActivityType.Running, 0, 3600, 9000.0, weightKg = 70.0)
        assertEquals((9.8 * 70).toInt(), log.kcal)
        assertEquals(listOf(log), repo.observeAll().first())
    }

    @Test
    fun `horas por tipo agrega varias actividades`() = runTest {
        val repo = InMemoryActivityRepository()
        val uc = LogActivityUseCase(repo)
        uc(ActivityType.Yoga, 0, 3600, 0.0)
        uc(ActivityType.Yoga, 10, 1800, 0.0)
        uc(ActivityType.Running, 20, 7200, 0.0)
        val hours = repo.observeHoursByType().first()
        assertEquals(1.5, hours.getValue(ActivityType.Yoga), 0.001)
        assertEquals(2.0, hours.getValue(ActivityType.Running), 0.001)
    }
}
