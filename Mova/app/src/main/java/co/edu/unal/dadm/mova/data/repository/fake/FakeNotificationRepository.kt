package co.edu.unal.dadm.mova.data.repository.fake

import co.edu.unal.dadm.mova.domain.model.Notification
import co.edu.unal.dadm.mova.domain.model.NotificationType
import co.edu.unal.dadm.mova.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.Duration
import java.time.Instant

/**
 * In-memory NotificationRepository (compartida por todos los usuarios, se pierde al cerrar la app).
 * Temporary fake until the Notifications module is implemented.
 */
class FakeNotificationRepository(now: Instant = Instant.now()) : NotificationRepository {
    private val notifications = MutableStateFlow(
        listOf(
            Notification("n1", NotificationType.AI_MESSAGE, "Your coach has a tip", "Try adding 10 g of protein to your breakfast.", now.minus(Duration.ofMinutes(20))),
            Notification("n2", NotificationType.HYDRATION_REMINDER, "Time to hydrate", "You are 750 ml away from today's water goal.", now.minus(Duration.ofHours(1))),
            Notification("n3", NotificationType.SCORE_INCREASED, "Uplift Score up!", "Your score went up 3 points this week.", now.minus(Duration.ofHours(3)), isRead = true),
            Notification("n4", NotificationType.WORKOUT_COMPLETE, "Workout complete", "Full Body Strength: 45 min, 320 kcal.", now.minus(Duration.ofDays(1)), isRead = true),
            Notification("n5", NotificationType.NUTRITION_UPGRADE, "New meal ideas", "We added meals that fit your diet.", now.minus(Duration.ofDays(2))),
            Notification("n6", NotificationType.FITNESS_DATA_READY, "Weekly report ready", "Your fitness data for last week is ready.", now.minus(Duration.ofDays(4)), isRead = true)
        )
    )

    override fun observeNotifications(userId: String): Flow<List<Notification>> = notifications.asStateFlow()

    override suspend fun markAsRead(notificationId: String) {
        notifications.update { list -> list.map { if (it.id == notificationId) it.copy(isRead = true) else it } }
    }

    override suspend fun markAllAsRead(userId: String) {
        notifications.update { list -> list.map { it.copy(isRead = true) } }
    }
}
