package co.edu.unal.dadm.mova.presentation.viewmodel.home

sealed interface NotificationsEvent {
    data class TabSelected(val index: Int) : NotificationsEvent
    data class NotificationClicked(val notificationId: String) : NotificationsEvent
    object MarkAllAsRead : NotificationsEvent
}

