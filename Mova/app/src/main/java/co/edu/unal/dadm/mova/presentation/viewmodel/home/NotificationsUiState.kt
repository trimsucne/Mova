package co.edu.unal.dadm.mova.presentation.viewmodel.home

import co.edu.unal.dadm.mova.domain.model.Notification

data class NotificationsUiState(
    val selectedTabIndex: Int = 0,
    val todayNotifications: List<Notification> = emptyList(),
    val pastNotifications: List<Notification> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

