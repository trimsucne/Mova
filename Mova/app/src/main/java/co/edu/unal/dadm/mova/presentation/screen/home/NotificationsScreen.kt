package co.edu.unal.dadm.mova.presentation.screen.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.unal.dadm.mova.domain.model.Notification
import co.edu.unal.dadm.mova.domain.model.NotificationType
import co.edu.unal.dadm.mova.presentation.components.MovaBottomBar
import co.edu.unal.dadm.mova.presentation.components.MovaIcons
import co.edu.unal.dadm.mova.presentation.components.SegmentedTabs
import co.edu.unal.dadm.mova.presentation.theme.MovaColor
import co.edu.unal.dadm.mova.presentation.viewmodel.home.NotificationsEvent
import co.edu.unal.dadm.mova.presentation.viewmodel.home.NotificationsViewModel

@Composable
fun NotificationsScreenRoute(
    userId: String,
    onNavigateTab: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: NotificationsViewModel = viewModel(
        factory = NotificationsViewModel.Factory(userId)
    )
    val uiState by viewModel.uiState.collectAsState()

    NotificationsScreen(
        uiState = uiState,
        onTabSelected = { index -> viewModel.onEvent(NotificationsEvent.TabSelected(index)) },
        onNotificationClick = { id -> viewModel.onEvent(NotificationsEvent.NotificationClicked(id)) },
        onMarkAllAsRead = { viewModel.onEvent(NotificationsEvent.MarkAllAsRead) },
        onNavigateTab = onNavigateTab,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    uiState: co.edu.unal.dadm.mova.presentation.viewmodel.home.NotificationsUiState,
    onTabSelected: (Int) -> Unit,
    onNotificationClick: (String) -> Unit,
    onMarkAllAsRead: () -> Unit,
    onNavigateTab: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val displayedNotifications = if (uiState.selectedTabIndex == 0) {
        uiState.todayNotifications
    } else {
        uiState.pastNotifications
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Notifications 🔔", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MovaColor.TextBlack)
                },
                actions = {
                    TextButton(onClick = onMarkAllAsRead) {
                        Text("Mark all read", color = MovaColor.PrimaryOrange, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MovaColor.BackgroundWhite)
            )
        },
        bottomBar = {
            MovaBottomBar(
                currentRoute = "notifications",
                onNavigate = onNavigateTab
            )
        },
        containerColor = MovaColor.BackgroundWhite,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SegmentedTabs(
                tabs = listOf("Today", "Past"),
                selectedTabIndex = uiState.selectedTabIndex,
                onTabSelected = onTabSelected
            )

            if (displayedNotifications.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No notifications in this tab.",
                        color = MovaColor.TextSecondary,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(displayedNotifications, key = { it.id }) { notification ->
                        NotificationCard(
                            notification = notification,
                            onClick = { onNotificationClick(notification.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationCard(
    notification: Notification,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (icon, iconBg, iconColor) = when (notification.type) {
        NotificationType.AI_MESSAGE -> Triple(MovaIcons.Flame, MovaColor.PrimaryOrangeLight, MovaColor.PrimaryOrange)
        NotificationType.SCORE_INCREASED -> Triple(MovaIcons.Star, MovaColor.ScoreGreenLight, MovaColor.ScoreGreen)
        NotificationType.HYDRATION_REMINDER -> Triple(MovaIcons.WaterDrop, MovaColor.HydrationBlueLight, MovaColor.HydrationBlue)
        NotificationType.WORKOUT_COMPLETE -> Triple(MovaIcons.Dumbbell, MovaColor.PrimaryOrangeLight, MovaColor.PrimaryOrange)
        NotificationType.NUTRITION_UPGRADE -> Triple(MovaIcons.Flame, MovaColor.CalorieLight, MovaColor.CalorieRed)
        NotificationType.FITNESS_DATA_READY -> Triple(MovaIcons.Info, MovaColor.CardBorder, MovaColor.TextBlack)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (!notification.isRead) MovaColor.PrimaryOrangeLight.copy(alpha = 0.3f) else MovaColor.CardLightGray
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notification.title,
                        fontWeight = if (!notification.isRead) FontWeight.Bold else FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = MovaColor.TextBlack,
                        modifier = Modifier.weight(1f)
                    )
                    if (!notification.isRead) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(MovaColor.PrimaryOrange)
                        )
                    }
                }

                Text(
                    text = notification.description,
                    fontSize = 13.sp,
                    color = MovaColor.TextDarkGray
                )
            }
        }
    }
}

