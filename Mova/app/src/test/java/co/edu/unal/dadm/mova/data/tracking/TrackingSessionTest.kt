package co.edu.unal.dadm.mova.data.tracking

import co.edu.unal.dadm.mova.domain.model.ActivityType
import co.edu.unal.dadm.mova.domain.model.TrackingStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TrackingSessionTest {
    // 0.0001 grados de latitud ~ 11.1 m
    private val step = 0.0001

    private fun session() = TrackingSession(clock = { 1_000L }).also { it.start(ActivityType.Running) }

    @Test
    fun `acumula distancia entre puntos validos`() {
        val s = session()
        assertTrue(s.onLocation(4.0, -74.0, 0, 5f))
        assertTrue(s.onLocation(4.0 + step, -74.0, 5_000, 5f))
        assertTrue(s.onLocation(4.0 + 2 * step, -74.0, 10_000, 5f))
        val state = s.state.value
        assertEquals(3, state.route.size)
        assertEquals(22.2, state.distanceMeters, 0.5)
    }

    @Test
    fun `descarta lecturas con mala precision`() {
        val s = session()
        assertFalse(s.onLocation(4.0, -74.0, 0, 80f))
        assertTrue(s.state.value.route.isEmpty())
    }

    @Test
    fun `descarta saltos de velocidad imposibles`() {
        val s = session()
        s.onLocation(4.0, -74.0, 0, 5f)
        // ~1.1 km en 1 s
        assertFalse(s.onLocation(4.01, -74.0, 1_000, 5f))
        assertEquals(0.0, s.state.value.distanceMeters, 0.0)
    }

    @Test
    fun `descarta ruido menor al paso minimo`() {
        val s = session()
        s.onLocation(4.0, -74.0, 0, 5f)
        assertFalse(s.onLocation(4.0 + 0.00001, -74.0, 5_000, 5f)) // ~1.1 m
    }

    @Test
    fun `en pausa no cuenta tiempo ni ubicacion`() {
        val s = session()
        s.tick()
        s.pause()
        s.tick()
        assertFalse(s.onLocation(4.0, -74.0, 0, 5f))
        assertEquals(1, s.state.value.elapsedSec)
        assertEquals(TrackingStatus.Paused, s.state.value.status)
        s.resume()
        s.tick()
        assertEquals(2, s.state.value.elapsedSec)
    }

    @Test
    fun `finish devuelve el estado final y reinicia`() {
        val s = session()
        repeat(3) { s.tick() }
        val final = s.finish()
        assertNotNull(final)
        assertEquals(3, final!!.elapsedSec)
        assertEquals(TrackingStatus.Idle, s.state.value.status)
        assertNull(s.finish())
    }

    @Test
    fun `ritmo solo con distancia suficiente`() {
        val s = session()
        assertNull(s.state.value.paceMinPerKm)
    }
}
