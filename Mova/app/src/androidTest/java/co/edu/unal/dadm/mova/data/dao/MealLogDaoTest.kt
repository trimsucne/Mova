package co.edu.unal.dadm.mova.data.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import co.edu.unal.dadm.mova.data.database.MovaDatabase
import co.edu.unal.dadm.mova.data.entity.MealLogEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MealLogDaoTest {
    private lateinit var db: MovaDatabase
    private lateinit var dao: MealLogDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), MovaDatabase::class.java).build()
        dao = db.mealLogDao()
    }

    @After
    fun tearDown() = db.close()

    private fun log(id: String, userId: String = "u1", date: String = "2026-11-02", deleted: Boolean = false, sync: String = "SYNCED", updatedAt: Long = 1) =
        MealLogEntity(id, userId, date, "LUNCH", "MANUAL", null, "X", null, 1.0, 1.0, 1.0, 17, 1, updatedAt, deleted, sync)

    @Test
    fun observeByDateFiltersUserDateAndDeleted() = runTest {
        dao.upsertAll(listOf(log("a"), log("b", deleted = true), log("c", userId = "u2"), log("d", date = "2026-11-01")))
        assertEquals(listOf("a"), dao.observeByDate("u1", "2026-11-02").first().map { it.id })
    }

    @Test
    fun observeRangeIncludesBothEnds() = runTest {
        dao.upsertAll(listOf(log("a", date = "2026-11-01"), log("b", date = "2026-11-30"), log("c", date = "2026-12-01")))
        assertEquals(listOf("a", "b"), dao.observeRange("u1", "2026-11-01", "2026-11-30").first().map { it.id })
    }

    @Test
    fun markSyncedIgnoresStaleUpdate() = runTest {
        dao.upsert(log("a", sync = "PENDING", updatedAt = 5))
        dao.markSynced("a", updatedAt = 4)
        assertEquals("PENDING", dao.getById("a")!!.syncState)
        dao.markSynced("a", updatedAt = 5)
        assertEquals("SYNCED", dao.getById("a")!!.syncState)
    }

    @Test
    fun deleteSyncedForUserKeepsPending() = runTest {
        dao.upsertAll(listOf(log("a"), log("b", sync = "PENDING"), log("c", userId = "u2")))
        dao.deleteSyncedForUser("u1")
        assertEquals(listOf("b"), dao.getPending("u1").map { it.id })
        assertEquals(1, dao.countForUser("u2"))
    }
}
