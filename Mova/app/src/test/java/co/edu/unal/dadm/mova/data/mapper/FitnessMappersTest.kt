package co.edu.unal.dadm.mova.data.mapper

import co.edu.unal.dadm.mova.data.dao.WorkoutWithExercises
import co.edu.unal.dadm.mova.domain.model.ActivityLog
import co.edu.unal.dadm.mova.domain.model.ActivityType
import co.edu.unal.dadm.mova.domain.model.RoutePoint
import co.edu.unal.dadm.mova.domain.model.WorkoutSeed
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FitnessMappersTest {
    @Test
    fun `ruta se codifica y decodifica sin perdida`() {
        val route = listOf(RoutePoint(4.6382, -74.084, 1000), RoutePoint(4.6383, -74.0841, 6000))
        assertEquals(route, decodeRoute(encodeRoute(route)))
    }

    @Test
    fun `ruta vacia y datos corruptos no fallan`() {
        assertTrue(decodeRoute("").isEmpty())
        assertEquals(1, decodeRoute("4.0,-74.0,1;basura;1,2").size)
    }

    @Test
    fun `activity log hace round trip por la entidad`() {
        val log = ActivityLog(
            "a1", ActivityType.Biking, 123L, 3600, 15000.0, 525, 140,
            listOf(RoutePoint(1.0, 2.0, 3))
        )
        assertEquals(log, log.toEntity().toDomain())
    }

    @Test
    fun `workout hace round trip y conserva el orden de ejercicios`() {
        val workout = WorkoutSeed.workouts.first { it.id == "back" }
        val (entity, exercises) = workout.toEntities()
        // Se barajan para comprobar que el mapper ordena por position.
        val restored = WorkoutWithExercises(entity, exercises.reversed()).toDomain()
        assertEquals(workout, restored)
    }
}
