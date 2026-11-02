package co.edu.unal.dadm.mova.data.repository

import co.edu.unal.dadm.mova.data.dao.MealDao
import co.edu.unal.dadm.mova.data.dao.MealLogDao
import co.edu.unal.dadm.mova.data.entity.MealEntity
import co.edu.unal.dadm.mova.data.entity.SyncState
import co.edu.unal.dadm.mova.data.mapper.toEntity
import co.edu.unal.dadm.mova.data.remote.RemoteChange
import co.edu.unal.dadm.mova.testutil.TestMeals
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class MealSyncCoordinatorTest {
    private val dao = FakeMealLogDao()
    private val remote = FakeMealLogRemote()
    private val date = LocalDate.of(2026, 11, 2)
    private val noMeals = object : MealDao {
        override fun observeAll(): Flow<List<MealEntity>> = emptyFlow()
        override fun observeById(id: String): Flow<MealEntity?> = emptyFlow()
        override suspend fun insertAll(meals: List<MealEntity>) = Unit
        override suspend fun count() = 0
    }

    @Test
    fun `downloads on sign in, applies remote changes and cleans up on sign out`() = runTest {
        remote.docs["r1"] = TestMeals.log("r1", date).toEntity(SyncState.SYNCED)
        // backgroundScope no avanza con advanceUntilIdle (ver Task 8): scope hijo propio, cancelado al final.
        val job = Job()
        val scope = CoroutineScope(coroutineContext + job)
        try {
        val repo = MealRepositoryImpl(noMeals, dao, remote, {}, scope)
        val userIds = MutableStateFlow<String?>("u1")
        MealSyncCoordinator(repo, userIds, scope).start()
        advanceUntilIdle()
        assertEquals("SYNCED", dao.getById("r1")!!.syncState)

        remote.changes.emit(listOf(RemoteChange.Upserted(TestMeals.log("r2", date).toEntity(SyncState.SYNCED))))
        advanceUntilIdle()
        assertEquals("r2", dao.getById("r2")!!.id)

        userIds.value = null
        advanceUntilIdle()
        assertNull(dao.getById("r1"))
        assertNull(dao.getById("r2"))
        } finally {
            job.cancel()
        }
    }

    @Test
    fun `keeps processing session changes when a step throws`() = runTest {
        remote.docs["r9"] = TestMeals.log("r9", date).toEntity(SyncState.SYNCED)
        val failingDao = object : MealLogDao by dao {
            override suspend fun deleteSyncedForUser(userId: String) = throw IllegalStateException("db down")
        }
        val job = Job()
        val scope = CoroutineScope(coroutineContext + job)
        try {
            val repo = MealRepositoryImpl(noMeals, failingDao, remote, {}, scope)
            val userIds = MutableStateFlow<String?>("u1")
            MealSyncCoordinator(repo, userIds, scope).start()
            advanceUntilIdle()
            userIds.value = null // onSignOut -> deleteSyncedForUser lanza
            advanceUntilIdle()
            userIds.value = "u2"
            advanceUntilIdle()
            assertEquals("SYNCED", dao.getById("r9")!!.syncState)
        } finally {
            job.cancel()
        }
    }
}
