package co.edu.unal.dadm.mova.presentation.viewmodel.places

import co.edu.unal.dadm.mova.domain.model.GeoPoint
import co.edu.unal.dadm.mova.domain.model.Gym
import co.edu.unal.dadm.mova.domain.model.RoutePlan
import co.edu.unal.dadm.mova.domain.model.RouteStep
import co.edu.unal.dadm.mova.domain.repository.DirectionsRepository
import co.edu.unal.dadm.mova.domain.repository.PlacesRepository
import co.edu.unal.dadm.mova.testutil.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DirectionsViewModelTest {
    @get:Rule val mainRule = MainDispatcherRule(UnconfinedTestDispatcher())

    private val gym = Gym("g1", "Campus Fitness", "Calle 1", 4.6486, -74.0721)

    private class FakePlaces(private val gym: Gym) : PlacesRepository {
        override fun observeGyms(): Flow<List<Gym>> = flowOf(listOf(gym))
        override fun observeGym(id: String): Flow<Gym?> = flowOf(gym)
    }

    private class FakeDirections(var result: RoutePlan?) : DirectionsRepository {
        var calls = 0
        override suspend fun walkingRoute(fromLat: Double, fromLng: Double, toLat: Double, toLng: Double): RoutePlan? {
            calls++
            return result
        }
    }

    private val plan = RoutePlan(
        points = listOf(GeoPoint(4.6355, -74.0817), GeoPoint(4.6486, -74.0721)),
        distanceMeters = 1500.0,
        durationSec = 1080.0,
        steps = listOf(RouteStep("Head north on Calle 26", 400.0), RouteStep("Turn right onto Carrera 30", 1100.0))
    )

    private fun TestScope.vm(directions: FakeDirections) =
        DirectionsViewModel("g1", FakePlaces(gym), directions).also { it.state.launchIn(backgroundScope) }

    @Test
    fun `con ruta usa distancia, ETA e instruccion de la ruta`() = runTest(mainRule.dispatcher) {
        val directions = FakeDirections(plan)
        val vm = vm(directions)

        vm.onEvent(DirectionsEvent.LocationUpdated(4.6355, -74.0817))

        val s = vm.state.value
        assertEquals(1500.0, s.distanceMeters!!, 1e-9)
        assertEquals(18, s.etaMinutes)
        assertEquals(2, s.route.size)
        assertTrue(s.instruction!!.endsWith("Turn right onto Carrera 30"))
    }

    @Test
    fun `si el servicio falla cae a linea recta`() = runTest(mainRule.dispatcher) {
        val vm = vm(FakeDirections(null))

        vm.onEvent(DirectionsEvent.LocationUpdated(4.6355, -74.0817))

        val s = vm.state.value
        assertTrue(s.route.isEmpty())
        assertTrue(s.distanceMeters!! > 0)
        assertTrue(s.instruction!!.startsWith("Head "))
        assertTrue(!s.routeLoading)
    }

    @Test
    fun `no recalcula la ruta por movimientos pequenos`() = runTest(mainRule.dispatcher) {
        val directions = FakeDirections(plan)
        val vm = vm(directions)

        vm.onEvent(DirectionsEvent.LocationUpdated(4.6355, -74.0817))
        vm.onEvent(DirectionsEvent.LocationUpdated(4.63552, -74.08172)) // ~3 m
        assertEquals(1, directions.calls)

        vm.onEvent(DirectionsEvent.LocationUpdated(4.6400, -74.0800)) // ~550 m
        assertEquals(2, directions.calls)
    }
}
