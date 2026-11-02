package co.edu.unal.dadm.mova.presentation.viewmodel.home

import co.edu.unal.dadm.mova.domain.model.ActivityLog
import co.edu.unal.dadm.mova.domain.model.MealLog
import co.edu.unal.dadm.mova.domain.model.Workout

data class HomeUiState(
    val userName: String = "Athlete",
    val avatarUrl: String? = null,
    val unreadNotificationsCount: Int = 2,
    val searchQuery: String = "",
    val consumedCalories: Int = 1730,
    val calorieGoal: Int = 2200,
    val remainingCalories: Int = 470,
    val hydrationCurrentMl: Int = 1750,
    val hydrationGoalMl: Int = 2500,
    val upliftScore: Int = 84,
    val workouts: List<Workout> = emptyList(),
    val todayMeals: List<MealLog> = emptyList(),
    val todayActivities: List<ActivityLog> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

