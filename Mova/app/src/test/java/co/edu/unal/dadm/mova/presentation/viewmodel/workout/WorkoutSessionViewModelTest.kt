package co.edu.unal.dadm.mova.presentation.viewmodel.workout

import co.edu.unal.dadm.mova.data.repository.InMemoryWorkoutRepository
import co.edu.unal.dadm.mova.domain.model.WorkoutSeed
import co.edu.unal.dadm.mova.domain.usecase.CompleteWorkoutSessionUseCase
import co.edu.unal.dadm.mova.testutil.MainDispatcherRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class WorkoutSessionViewModelTest {
    @get:Rule val mainRule = MainDispatcherRule()

    private val repo = InMemoryWorkoutRepository()
    private val workout = WorkoutSeed.workouts.first { it.id == "back" } // 6 ejercicios x 3 sets

    private fun vm() = WorkoutSessionViewModel("back", repo, CompleteWorkoutSessionUseCase(repo))

    @Test
    fun `carga el workout`() {
        val s = vm().state.value
        assertFalse(s.loading)
        assertEquals(workout.title, s.workout?.title)
        assertEquals(18, s.totalSets)
    }

    @Test
    fun `completar un set inicia descanso y bloquea otro set`() {
        val vm = vm()
        vm.onEvent(WorkoutSessionEvent.CompleteSet)
        var s = vm.state.value
        assertTrue(s.resting)
        assertEquals(30, s.restRemainingSec)
        assertEquals(1, s.completedSets)

        vm.onEvent(WorkoutSessionEvent.CompleteSet) // ignorado: descansando
        assertEquals(1, vm.state.value.completedSets)

        vm.onEvent(WorkoutSessionEvent.SkipRest)
        s = vm.state.value
        assertFalse(s.resting)
    }

    @Test
    fun `tick descuenta el descanso y termina solo`() {
        val vm = vm()
        vm.onEvent(WorkoutSessionEvent.CompleteSet)
        repeat(30) { vm.onEvent(WorkoutSessionEvent.Tick) }
        val s = vm.state.value
        assertFalse(s.resting)
        assertEquals(30, s.elapsedSec)
    }

    @Test
    fun `pausa detiene el cronometro`() {
        val vm = vm()
        vm.onEvent(WorkoutSessionEvent.Tick)
        vm.onEvent(WorkoutSessionEvent.TogglePause)
        vm.onEvent(WorkoutSessionEvent.Tick)
        assertEquals(1, vm.state.value.elapsedSec)
        vm.onEvent(WorkoutSessionEvent.TogglePause)
        vm.onEvent(WorkoutSessionEvent.Tick)
        assertEquals(2, vm.state.value.elapsedSec)
    }

    @Test
    fun `tres sets pasan al siguiente ejercicio`() {
        val vm = vm()
        repeat(3) {
            vm.onEvent(WorkoutSessionEvent.CompleteSet)
            vm.onEvent(WorkoutSessionEvent.SkipRest)
        }
        val s = vm.state.value
        assertEquals(1, s.exerciseIndex)
        assertEquals(0, s.setsDoneInExercise)
    }

    @Test
    fun `completar todo marca allDone y finish guarda la sesion`() {
        val vm = vm()
        repeat(18) {
            vm.onEvent(WorkoutSessionEvent.CompleteSet)
            vm.onEvent(WorkoutSessionEvent.SkipRest)
        }
        assertTrue(vm.state.value.allDone)
        assertNull(vm.state.value.savedSessionId)

        vm.onEvent(WorkoutSessionEvent.FinishClicked)
        val s = vm.state.value
        assertNotNull(s.savedSessionId)
        assertEquals(18, s.completedSets)
    }
}
