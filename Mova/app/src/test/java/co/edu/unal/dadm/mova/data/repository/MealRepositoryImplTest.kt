package co.edu.unal.dadm.mova.data.repository

import co.edu.unal.dadm.mova.core.common.Result
import co.edu.unal.dadm.mova.data.dao.MealDao
import co.edu.unal.dadm.mova.data.entity.MealEntity
import co.edu.unal.dadm.mova.data.entity.SyncState
import co.edu.unal.dadm.mova.data.mapper.toEntity
import co.edu.unal.dadm.mova.data.remote.RemoteChange
import co.edu.unal.dadm.mova.domain.model.Macros
import co.edu.unal.dadm.mova.domain.model.MealLog
import co.edu.unal.dadm.mova.domain.model.MealType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

class MealRepositoryImplTest {
    private val dao = FakeMealLogDao()
    private val remote = FakeMealLogRemote()
    private var scheduled = 0
    private val date = LocalDate.of(2026, 11, 2)

    private val noMeals = object : MealDao {
        override fun observeAll(): Flow<List<MealEntity>> = emptyFlow()
        override fun observeById(id: String): Flow<MealEntity?> = emptyFlow()
        override suspend fun insertAll(meals: List<MealEntity>) = Unit
        override suspend fun count() = 0
    }

    private fun TestScope.repository() =
        MealRepositoryImpl(noMeals, dao, remote, { scheduled++ }, this, now = { 9_000 })

    private fun entry(id: String = "a", userId: String = "u1", at: Long = 1_000) =
        MealLog.manual("X", Macros(10.0, 10.0, 10.0), userId, date, MealType.LUNCH, Instant.ofEpochMilli(at), id = id)

    @Test
    fun `log online ends synced and uploaded`() = runTest {
        val repo = repository()
        assertEquals(Result.Success("a"), repo.log(entry()))
        advanceUntilIdle()
        assertEquals("SYNCED", dao.getById("a")!!.syncState)
        assertTrue("a" in remote.docs)
    }

    @Test
    fun `log offline stays pending, visible and schedules worker`() = runTest {
        remote.offline = true
        val repo = repository()
        repo.log(entry())
        advanceUntilIdle()
        assertEquals("PENDING", dao.getById("a")!!.syncState)
        assertEquals(1, scheduled)
        assertEquals(1, repo.observeLogs("u1", date).first().size)
    }

    @Test
    fun `local user is never uploaded`() = runTest {
        val repo = repository()
        repo.log(entry(userId = MealLog.LOCAL_USER_ID))
        advanceUntilIdle()
        assertTrue(remote.docs.isEmpty())
        assertEquals(0, scheduled)
    }

    @Test
    fun `delete removes remote document and local row`() = runTest {
        val repo = repository()
        repo.log(entry())
        advanceUntilIdle()
        assertEquals(Result.Success(Unit), repo.delete("a"))
        advanceUntilIdle()
        assertFalse("a" in remote.docs)
        assertNull(dao.getById("a"))
    }

    @Test
    fun `delete offline hides the row until it syncs`() = runTest {
        val repo = repository()
        repo.log(entry())
        advanceUntilIdle()
        remote.offline = true
        repo.delete("a")
        advanceUntilIdle()
        assertTrue(dao.getById("a")!!.deleted)
        assertTrue(repo.observeLogs("u1", date).first().isEmpty())
        remote.offline = false
        assertTrue(repo.syncPending("u1"))
        assertNull(dao.getById("a"))
    }

    @Test
    fun `remote change wins only when newer`() = runTest {
        val repo = repository()
        repo.log(entry(at = 2_000))
        advanceUntilIdle()
        val older = dao.getById("a")!!.copy(name = "Old", updatedAt = 1_000)
        repo.applyRemote(RemoteChange.Upserted(older))
        assertEquals("X", dao.getById("a")!!.name)
        repo.applyRemote(RemoteChange.Upserted(older.copy(name = "New", updatedAt = 3_000)))
        assertEquals("New", dao.getById("a")!!.name)
    }

    @Test
    fun `remote removal deletes synced row`() = runTest {
        val repo = repository()
        repo.log(entry())
        advanceUntilIdle()
        repo.applyRemote(RemoteChange.Removed("a"))
        assertNull(dao.getById("a"))
    }

    @Test
    fun `download fills an empty phone`() = runTest {
        remote.docs["r1"] = entry(id = "r1").toEntity(SyncState.SYNCED)
        val repo = repository()
        repo.downloadIfEmpty("u1")
        assertEquals("SYNCED", dao.getById("r1")!!.syncState)
    }

    @Test
    fun `sign out keeps pending rows and drops synced ones`() = runTest {
        val repo = repository()
        repo.log(entry(id = "synced"))
        advanceUntilIdle()
        remote.offline = true
        repo.log(entry(id = "pending"))
        advanceUntilIdle()
        repo.onSignOut("u1")
        assertNull(dao.getById("synced"))
        assertEquals("PENDING", dao.getById("pending")!!.syncState)
    }
}
