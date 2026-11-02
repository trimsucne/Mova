package co.edu.unal.dadm.mova.domain.repository

import co.edu.unal.dadm.mova.domain.model.Notification
import kotlinx.coroutines.flow.Flow

/**
 * Notificaciones del usuario.
 *
 * Temporary fake until the Notifications module is implemented: solo lo que usa PR #5.
 */
interface NotificationRepository {
    fun observeNotifications(userId: String): Flow<List<Notification>>
    suspend fun markAsRead(notificationId: String)
    suspend fun markAllAsRead(userId: String)
}
