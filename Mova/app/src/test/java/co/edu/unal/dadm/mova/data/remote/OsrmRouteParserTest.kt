package co.edu.unal.dadm.mova.data.remote

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class OsrmRouteParserTest {
    private val sample = """
        {"code":"Ok","routes":[{"distance":1234.5,"duration":900.0,
          "geometry":{"type":"LineString","coordinates":[[-74.0817,4.6355],[-74.0800,4.6400],[-74.0721,4.6486]]},
          "legs":[{"steps":[
            {"distance":400.0,"name":"Calle 26","maneuver":{"type":"depart","bearing_after":45}},
            {"distance":800.0,"name":"Carrera 30","maneuver":{"type":"turn","modifier":"right"}},
            {"distance":0.0,"name":"","maneuver":{"type":"arrive"}}
          ]}]}]}
    """.trimIndent()

    @Test
    fun `parsea geometria con orden lat lng y totales`() {
        val plan = OsrmRouteParser.parse(sample)
        assertNotNull(plan)
        plan!!
        assertEquals(3, plan.points.size)
        assertEquals(4.6355, plan.points.first().lat, 1e-9)
        assertEquals(-74.0817, plan.points.first().lng, 1e-9)
        assertEquals(1234.5, plan.distanceMeters, 1e-9)
        assertEquals(900.0, plan.durationSec, 1e-9)
    }

    @Test
    fun `construye instrucciones legibles`() {
        val steps = OsrmRouteParser.parse(sample)!!.steps
        assertEquals("Head north-east on Calle 26", steps[0].instruction)
        assertEquals("Turn right onto Carrera 30", steps[1].instruction)
        assertEquals("Arrive at your destination", steps[2].instruction)
        assertEquals(400.0, steps[0].distanceMeters, 1e-9)
    }

    @Test
    fun `codigo distinto de Ok devuelve null`() {
        assertNull(OsrmRouteParser.parse("""{"code":"NoRoute","routes":[]}"""))
    }

    @Test
    fun `geometria con menos de dos puntos devuelve null`() {
        val json = """{"code":"Ok","routes":[{"distance":1,"duration":1,"geometry":{"coordinates":[[-74.0,4.6]]}}]}"""
        assertNull(OsrmRouteParser.parse(json))
    }
}
