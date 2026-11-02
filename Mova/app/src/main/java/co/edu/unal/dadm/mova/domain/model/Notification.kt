package co.edu.unal.dadm.mova.domain.model

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Notificación para el usuario (PR #5: Home y pantalla de Notificaciones).
 *
 * Temporary fake until the Notifications module is implemented.
 */
data class Notification(
    val id: String,
    val type: NotificationType,
    val title: String,
    val description: String,
    val createdAt: Instant,
    val isRead: Boolean = false
) {
    /** true si se creó hoy (zona horaria del dispositivo); separa las pestañas Today/Past. */
    val isToday: Boolean
        get() = createdAt.atZone(ZoneId.systemDefault()).toLocalDate() == LocalDate.now()
}
