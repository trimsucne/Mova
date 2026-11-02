package co.edu.unal.dadm.mova.data.repository

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

/**
 * Reacciona a los cambios de sesión (spec §5): al iniciar sesión descarga, sube pendientes y escucha
 * cambios remotos; al cerrar sesión limpia los registros sincronizados del usuario anterior.
 */
class MealSyncCoordinator(
    private val repository: MealRepositoryImpl,
    private val userIds: Flow<String?>,
    private val scope: CoroutineScope
) {
    fun start(): Job = scope.launch {
        var previous: String? = null
        var listener: Job? = null
        userIds.distinctUntilChanged().collect { userId ->
            listener?.cancel()
            previous?.let { safely { repository.onSignOut(it) } }
            if (userId != null) {
                safely { repository.downloadIfEmpty(userId) }
                safely { repository.syncPending(userId) }
                listener = launch { safely { repository.listenRemote(userId) } }
            }
            previous = userId
        }
    }

    // Un fallo de Room/red no debe tumbar el bucle ni la app; la cancelación sí se propaga.
    private suspend fun safely(block: suspend () -> Unit) {
        try {
            block()
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            // Se reintenta con la siguiente emisión de sesión o la sincronización periódica.
        }
    }
}
