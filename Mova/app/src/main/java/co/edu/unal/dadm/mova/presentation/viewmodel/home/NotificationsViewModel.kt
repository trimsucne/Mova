package co.edu.unal.dadm.mova.presentation.viewmodel.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import co.edu.unal.dadm.mova.core.di.RepositoryModule
import co.edu.unal.dadm.mova.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NotificationsViewModel(
    private val notificationRepository: NotificationRepository,
    private val userId: String
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(0)
    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    init {
        loadNotifications()
    }

    private fun loadNotifications() {
        viewModelScope.launch {
            notificationRepository.observeNotifications(userId).collect { list ->
                val today = list.filter { it.isToday }
                val past = list.filter { !it.isToday }
                _uiState.value = _uiState.value.copy(
                    todayNotifications = today,
                    pastNotifications = past,
                    isLoading = false
                )
            }
        }
    }

    fun onEvent(event: NotificationsEvent) {
        when (event) {
            is NotificationsEvent.TabSelected -> {
                _selectedTab.value = event.index
                _uiState.value = _uiState.value.copy(selectedTabIndex = event.index)
            }
            is NotificationsEvent.NotificationClicked -> {
                viewModelScope.launch {
                    notificationRepository.markAsRead(event.notificationId)
                }
            }
            is NotificationsEvent.MarkAllAsRead -> {
                viewModelScope.launch {
                    notificationRepository.markAllAsRead(userId)
                }
            }
        }
    }

    class Factory(private val userId: String) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return NotificationsViewModel(
                notificationRepository = RepositoryModule.notificationRepository,
                userId = userId
            ) as T
        }
    }
}

