package co.edu.unal.dadm.mova.presentation.viewmodel.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import co.edu.unal.dadm.mova.core.di.RepositoryModule
import co.edu.unal.dadm.mova.domain.repository.MealRepository
import co.edu.unal.dadm.mova.domain.repository.ProgressRepository
import co.edu.unal.dadm.mova.presentation.charts.WeeklyBarData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class UpliftScoreViewModel(
    private val progressRepository: ProgressRepository,
    private val mealRepository: MealRepository,
    private val userId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(UpliftScoreUiState())
    val uiState: StateFlow<UpliftScoreUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val weekStart = now - (7L * 24 * 60 * 60 * 1000)

            combine(
                progressRepository.observeLatestScore(userId),
                progressRepository.observeWeeklyScores(userId),
                mealRepository.observeLogs(userId, weekStart, now)
            ) { latest, weeklyList, mealLogs ->
                val bars = if (weeklyList.isNotEmpty()) {
                    weeklyList.map { snapshot ->
                        WeeklyBarData(
                            dayLabel = snapshot.dayOfWeek,
                            score = snapshot.score,
                            isHighlighted = snapshot.dayOfWeek == (latest?.dayOfWeek ?: "Sun")
                        )
                    }
                } else {
                    listOf(
                        WeeklyBarData("Mon", 78),
                        WeeklyBarData("Tue", 80),
                        WeeklyBarData("Wed", 79),
                        WeeklyBarData("Thu", 82),
                        WeeklyBarData("Fri", 81),
                        WeeklyBarData("Sat", 85),
                        WeeklyBarData("Sun", 84, isHighlighted = true)
                    )
                }

                val adherence = if (mealLogs.isNotEmpty()) ((mealLogs.size.toFloat() / 21f) * 100).coerceAtMost(100f).toInt() else 85

                UpliftScoreUiState(
                    score = latest?.score ?: 84,
                    healthStatus = latest?.healthStatus ?: "Optimal Health",
                    strength = latest?.strength ?: 88,
                    agility = latest?.agility ?: 76,
                    endurance = latest?.endurance ?: 82,
                    weeklyProgress = bars,
                    weeklyNutritionalAdherencePercent = adherence,
                    loggedMealsCount = mealLogs.size,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun onEvent(event: UpliftScoreEvent) {
        when (event) {
            is UpliftScoreEvent.Refresh -> loadData()
        }
    }

    class Factory(private val userId: String) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return UpliftScoreViewModel(
                progressRepository = RepositoryModule.progressRepository,
                mealRepository = RepositoryModule.mealRepository,
                userId = userId
            ) as T
        }
    }
}

