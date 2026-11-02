package co.edu.unal.dadm.mova.presentation.viewmodel.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import co.edu.unal.dadm.mova.core.di.RepositoryModule
import co.edu.unal.dadm.mova.domain.model.ActivityLog
import co.edu.unal.dadm.mova.domain.model.MealLog
import co.edu.unal.dadm.mova.domain.model.Notification
import co.edu.unal.dadm.mova.domain.model.ScoreSnapshot
import co.edu.unal.dadm.mova.domain.model.UserProfile
import co.edu.unal.dadm.mova.domain.model.Workout
import co.edu.unal.dadm.mova.domain.repository.ActivityRepository
import co.edu.unal.dadm.mova.domain.repository.HydrationRepository
import co.edu.unal.dadm.mova.domain.repository.MealRepository
import co.edu.unal.dadm.mova.domain.repository.NotificationRepository
import co.edu.unal.dadm.mova.domain.repository.ProfileRepository
import co.edu.unal.dadm.mova.domain.repository.ProgressRepository
import co.edu.unal.dadm.mova.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class HomeViewModel(
    private val profileRepository: ProfileRepository,
    private val mealRepository: MealRepository,
    private val hydrationRepository: HydrationRepository,
    private val progressRepository: ProgressRepository,
    private val workoutRepository: WorkoutRepository,
    private val activityRepository: ActivityRepository,
    private val notificationRepository: NotificationRepository,
    private val userId: String
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeData()
    }

    private fun loadHomeData() {
        viewModelScope.launch {
            combine(
                profileRepository.observe(userId),
                mealRepository.observeTodayLogs(userId),
                mealRepository.getCalorieGoal(userId),
                hydrationRepository.observeTodayWater(userId),
                hydrationRepository.getWaterGoal(userId),
                progressRepository.observeLatestScore(userId),
                workoutRepository.observeWorkouts(),
                activityRepository.observeTodayActivities(userId),
                notificationRepository.observeNotifications(userId),
                _searchQuery
            ) { args: Array<Any?> ->
                val profile = args[0] as? UserProfile
                @Suppress("UNCHECKED_CAST")
                val meals = (args[1] as? List<MealLog>).orEmpty()
                val calorieGoal = args[2] as? Int ?: 2200
                val waterCurrent = args[3] as? Int ?: 1750
                val waterGoal = args[4] as? Int ?: 2500
                val scoreSnapshot = args[5] as? ScoreSnapshot
                @Suppress("UNCHECKED_CAST")
                val workouts = (args[6] as? List<Workout>).orEmpty()
                @Suppress("UNCHECKED_CAST")
                val activities = (args[7] as? List<ActivityLog>).orEmpty()
                @Suppress("UNCHECKED_CAST")
                val notifications = (args[8] as? List<Notification>).orEmpty()
                val query = args[9] as? String ?: ""

                val consumed = meals.sumOf { it.calories }
                val targetCal = if (calorieGoal > 0) calorieGoal else 2200
                val remaining = (targetCal - consumed).coerceAtLeast(0)
                val unreadCount = notifications.count { !it.isRead }

                HomeUiState(
                    userName = profile?.displayName?.ifBlank { "Athlete" } ?: "Athlete",
                    avatarUrl = profile?.avatarUrl,
                    unreadNotificationsCount = unreadCount,
                    searchQuery = query,
                    consumedCalories = consumed,
                    calorieGoal = targetCal,
                    remainingCalories = remaining,
                    hydrationCurrentMl = waterCurrent,
                    hydrationGoalMl = waterGoal,
                    upliftScore = scoreSnapshot?.score ?: 84,
                    workouts = workouts,
                    todayMeals = meals,
                    todayActivities = activities,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun onEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.SearchQueryChanged -> {
                _searchQuery.value = event.query
            }
            else -> {}
        }
    }

    class Factory(
        private val context: android.content.Context,
        private val userId: String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(
                profileRepository = RepositoryModule.profileRepository(context),
                mealRepository = RepositoryModule.mealRepository,
                hydrationRepository = RepositoryModule.hydrationRepository,
                progressRepository = RepositoryModule.progressRepository,
                workoutRepository = RepositoryModule.workoutRepository,
                activityRepository = RepositoryModule.activityRepository,
                notificationRepository = RepositoryModule.notificationRepository,
                userId = userId
            ) as T
        }
    }

}


