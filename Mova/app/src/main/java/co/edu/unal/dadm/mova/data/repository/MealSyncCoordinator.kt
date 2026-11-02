package co.edu.unal.dadm.mova.data.repository

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
            previous?.let { repository.onSignOut(it) }
            if (userId != null) {
                repository.downloadIfEmpty(userId)
                repository.syncPending(userId)
                listener = launch { repository.listenRemote(userId) }
            }
            previous = userId
        }
    }
}
