package co.edu.unal.dadm.mova.data.repository

import co.edu.unal.dadm.mova.core.common.Result
import co.edu.unal.dadm.mova.data.dao.MealDao
import co.edu.unal.dadm.mova.data.dao.MealLogDao
import co.edu.unal.dadm.mova.data.entity.MealLogEntity
import co.edu.unal.dadm.mova.data.entity.SyncState
import co.edu.unal.dadm.mova.data.mapper.toDomain
import co.edu.unal.dadm.mova.data.mapper.toEntity
import co.edu.unal.dadm.mova.data.remote.MealLogRemoteDataSource
import co.edu.unal.dadm.mova.data.remote.RemoteChange
import co.edu.unal.dadm.mova.domain.model.Meal
import co.edu.unal.dadm.mova.domain.model.MealLog
import co.edu.unal.dadm.mova.domain.repository.MealRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.time.LocalDate

/**
 * Room es lo que leen las pantallas; Firestore es la copia remota (spec de Nutrición §5).
 * Las escrituras se guardan primero en Room y se suben en [scope] sin bloquear la UI.
 */
class MealRepositoryImpl(
    private val mealDao: MealDao,
    private val logDao: MealLogDao,
    private val remote: MealLogRemoteDataSource,
    private val scheduler: SyncScheduler,
    private val scope: CoroutineScope,
    private val pushTimeoutMs: Long = 5_000,
    private val now: () -> Long = System::currentTimeMillis
) : MealRepository {

    override fun observeCatalog(): Flow<List<Meal>> =
        mealDao.observeAll().map { meals -> meals.map { it.toDomain() } }

    override fun observeMeal(id: String): Flow<Meal?> =
        mealDao.observeById(id).map { it?.toDomain() }

    override fun observeLogs(userId: String, date: LocalDate): Flow<List<MealLog>> =
        logDao.observeByDate(userId, date.toString()).map { logs -> logs.map { it.toDomain() } }

    override fun observeLogs(userId: String, from: LocalDate, to: LocalDate): Flow<List<MealLog>> =
        logDao.observeRange(userId, from.toString(), to.toString()).map { logs -> logs.map { it.toDomain() } }

    override suspend fun log(entry: MealLog): Result<String> = try {
        val entity = entry.toEntity(SyncState.PENDING)
        logDao.upsert(entity)
        scope.launch { pushOrSchedule(entity) }
        Result.Success(entry.id)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.Error(e.message ?: "Could not save the meal")
    }

    override suspend fun delete(logId: String): Result<Unit> = try {
        val current = logDao.getById(logId)
        if (current == null) {
            Result.Error("Meal log not found")
        } else if (current.userId == MealLog.LOCAL_USER_ID) {
            logDao.hardDelete(logId)
            Result.Success(Unit)
        } else {
            val tombstone = current.copy(deleted = true, updatedAt = now(), syncState = SyncState.PENDING.name)
            logDao.upsert(tombstone)
            scope.launch { pushOrSchedule(tombstone) }
            Result.Success(Unit)
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.Error(e.message ?: "Could not delete the meal")
    }

    /** Sube los pendientes del usuario y se detiene en el primer fallo. Devuelve false si alguno falló. */
    suspend fun syncPending(userId: String): Boolean {
        if (userId == MealLog.LOCAL_USER_ID) return true
        for (entity in logDao.getPending(userId)) {
            try {
                push(entity)
            } catch (_: TimeoutCancellationException) {
                return false
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                return false
            }
        }
        return true
    }

    /** Descarga inicial: solo si el teléfono no tiene registros de ese usuario. */
    suspend fun downloadIfEmpty(userId: String) {
        if (userId == MealLog.LOCAL_USER_ID || logDao.countForUser(userId) > 0) return
        try {
            logDao.upsertAll(remote.fetchAll(userId).map { it.copy(syncState = SyncState.SYNCED.name) })
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            // Sin conexión: el listener completa los datos cuando vuelva la red.
        }
    }

    /** Escucha cambios de otros dispositivos hasta que se cancele la corrutina. */
    suspend fun listenRemote(userId: String) {
        if (userId == MealLog.LOCAL_USER_ID) return
        try {
            remote.listen(userId).collect { changes -> changes.forEach { applyRemote(it) } }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            // El listener se reinicia en el próximo inicio de sesión.
        }
    }

    /** Last-write-wins por updatedAt. */
    suspend fun applyRemote(change: RemoteChange) {
        when (change) {
            is RemoteChange.Upserted -> {
                val local = logDao.getById(change.log.id)
                if (local == null || change.log.updatedAt > local.updatedAt) {
                    logDao.upsert(change.log.copy(syncState = SyncState.SYNCED.name))
                }
            }
            is RemoteChange.Removed -> {
                val local = logDao.getById(change.id)
                if (local != null && local.syncState == SyncState.SYNCED.name) logDao.hardDelete(change.id)
            }
        }
    }

    /** Intenta subir los pendientes y borra de Room solo lo ya sincronizado. */
    suspend fun onSignOut(userId: String) {
        syncPending(userId)
        logDao.deleteSyncedForUser(userId)
    }

    private suspend fun pushOrSchedule(entity: MealLogEntity) {
        if (entity.userId == MealLog.LOCAL_USER_ID) return
        try {
            push(entity)
        } catch (_: TimeoutCancellationException) {
            scheduler.schedule()
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            scheduler.schedule()
        }
    }

    private suspend fun push(entity: MealLogEntity) {
        withTimeout(pushTimeoutMs) {
            if (entity.deleted) {
                remote.delete(entity.userId, entity.id)
                logDao.hardDelete(entity.id)
            } else {
                remote.upsert(entity)
                logDao.markSynced(entity.id, entity.updatedAt)
            }
        }
    }
}
