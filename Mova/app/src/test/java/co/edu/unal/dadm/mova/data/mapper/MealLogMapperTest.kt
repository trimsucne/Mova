package co.edu.unal.dadm.mova.data.mapper

import co.edu.unal.dadm.mova.data.entity.SyncState
import co.edu.unal.dadm.mova.domain.model.Macros
import co.edu.unal.dadm.mova.domain.model.MealLog
import co.edu.unal.dadm.mova.domain.model.MealType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

class MealLogMapperTest {
    @Test
    fun `round trip keeps every field`() {
        val log = MealLog.manual("Arepa", Macros(5.0, 30.0, 8.0), "u1", LocalDate.of(2026, 11, 2), MealType.BREAKFAST, Instant.ofEpochMilli(1_000), id = "id1")
        val entity = log.toEntity(SyncState.PENDING)
        assertEquals("2026-11-02", entity.date)
        assertEquals("PENDING", entity.syncState)
        assertFalse(entity.deleted)
        assertEquals(log, entity.toDomain())
    }
}
