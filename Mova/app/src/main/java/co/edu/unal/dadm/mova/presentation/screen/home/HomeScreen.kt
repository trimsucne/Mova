package co.edu.unal.dadm.mova.presentation.screen.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.unal.dadm.mova.presentation.components.CategoryChip
import co.edu.unal.dadm.mova.presentation.components.MovaBottomBar
import co.edu.unal.dadm.mova.presentation.components.MovaIcons
import co.edu.unal.dadm.mova.presentation.screen.home.components.*
import co.edu.unal.dadm.mova.presentation.theme.MovaColor
import co.edu.unal.dadm.mova.presentation.viewmodel.home.HomeEvent
import co.edu.unal.dadm.mova.presentation.viewmodel.home.HomeViewModel

@Composable
fun HomeScreenRoute(
    userId: String,
    onNavigateToNotifications: () -> Unit,
    onNavigateToSearch: (String) -> Unit,
    onNavigateToScore: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToTab: (String) -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToCalories: () -> Unit = {},
    onNavigateToMeals: () -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel: HomeViewModel = viewModel(
        factory = HomeViewModel.Factory(context, userId)
    )
    val uiState by viewModel.uiState.collectAsState()

    HomeScreen(
        uiState = uiState,
        onSearchQueryChange = { query ->
            viewModel.onEvent(HomeEvent.SearchQueryChanged(query))
            if (query.length >= 2) {
                onNavigateToSearch(query)
            }
        },
        onSearchSubmitted = { query ->
            onNavigateToSearch(query)
        },
        onNotificationsClick = onNavigateToNotifications,
        onProfileClick = onNavigateToProfile,
        onCategoryScoreClick = onNavigateToScore,
        onCategoryHydrationClick = {},
        onCategoryCalorieClick = onNavigateToCalories,
        onWorkoutClick = { _ -> },
        onSeeAllMealsClick = onNavigateToMeals,
        onAskCoachClick = {},
        onNavigateTab = onNavigateToTab,
        modifier = modifier
    )
}

@Composable
fun HomeScreen(
    uiState: co.edu.unal.dadm.mova.presentation.viewmodel.home.HomeUiState,
    onSearchQueryChange: (String) -> Unit,
    onSearchSubmitted: (String) -> Unit,
    onNotificationsClick: () -> Unit,
    onProfileClick: () -> Unit,
    onCategoryScoreClick: () -> Unit,
    onCategoryHydrationClick: () -> Unit,
    onCategoryCalorieClick: () -> Unit,
    onWorkoutClick: (String) -> Unit,
    onSeeAllMealsClick: () -> Unit,
    onAskCoachClick: () -> Unit,
    onNavigateTab: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            MovaBottomBar(
                currentRoute = "home",
                onNavigate = onNavigateTab,
                unreadNotifications = uiState.unreadNotificationsCount
            )
        },
        containerColor = MovaColor.BackgroundWhite
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            HomeHeader(
                userName = uiState.userName,
                avatarUrl = uiState.avatarUrl,
                unreadNotificationsCount = uiState.unreadNotificationsCount,
                searchQuery = uiState.searchQuery,
                onSearchQueryChange = onSearchQueryChange,
                onSearchSubmitted = onSearchSubmitted,
                onNotificationsClick = onNotificationsClick,
                onProfileClick = onProfileClick
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CategoryChip(
                    title = "Hydration",
                    value = "${uiState.hydrationCurrentMl} ml",
                    subtitle = "Goal ${uiState.hydrationGoalMl} ml",
                    icon = MovaIcons.WaterDrop,
                    containerColor = MovaColor.HydrationBlueLight,
                    iconColor = MovaColor.HydrationBlue,
                    onClick = onCategoryHydrationClick,
                    modifier = Modifier.weight(1f)
                )

                CategoryChip(
                    title = "Score",
                    value = "${uiState.upliftScore}",
                    subtitle = "Top 10%",
                    icon = MovaIcons.Star,
                    containerColor = MovaColor.ScoreGreenLight,
                    iconColor = MovaColor.ScoreGreen,
                    onClick = onCategoryScoreClick,
                    modifier = Modifier.weight(1f)
                )

                CategoryChip(
                    title = "Calorie",
                    value = "${uiState.consumedCalories}",
                    subtitle = "Target ${uiState.calorieGoal}",
                    icon = MovaIcons.Flame,
                    containerColor = MovaColor.CalorieLight,
                    iconColor = MovaColor.CalorieRed,
                    onClick = onCategoryCalorieClick,
                    modifier = Modifier.weight(1f)
                )
            }

            CalorieGoalCard(
                consumedCalories = uiState.consumedCalories,
                calorieGoal = uiState.calorieGoal,
                remainingCalories = uiState.remainingCalories,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            WorkoutCard(
                workouts = uiState.workouts,
                onWorkoutClick = onWorkoutClick,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            CoachCard(
                onAskCoachClick = onAskCoachClick,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            DietCard(
                meals = uiState.todayMeals,
                onSeeAllClick = onSeeAllMealsClick,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            ActivitiesCard(
                activities = uiState.todayActivities,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

