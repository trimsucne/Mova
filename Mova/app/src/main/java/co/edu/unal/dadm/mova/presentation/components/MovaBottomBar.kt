package co.edu.unal.dadm.mova.presentation.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import co.edu.unal.dadm.mova.presentation.theme.MovaColor

@Composable
fun MovaBottomBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
    unreadNotifications: Int = 0
) {
    val items = listOf(
        BottomBarItem(route = "home", title = "Home", icon = MovaIcons.Home),
        BottomBarItem(route = "uplift_score", title = "Score", icon = MovaIcons.Star),
        BottomBarItem(route = "notifications", title = "Notifications", icon = MovaIcons.Bell, badgeCount = unreadNotifications),
        BottomBarItem(route = "profile", title = "Profile", icon = MovaIcons.Person)
    )

    NavigationBar(
        modifier = modifier,
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        items.forEach { item ->
            val isSelected = currentRoute == item.route
            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(item.route) },
                icon = {
                    BadgedBox(
                        badge = {
                            if (item.badgeCount > 0) {
                                Badge(containerColor = MovaColor.PrimaryOrange) {
                                    Text(item.badgeCount.toString(), color = Color.White)
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.title
                        )
                    }
                },
                label = { Text(item.title) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MovaColor.PrimaryOrange,
                    selectedTextColor = MovaColor.PrimaryOrange,
                    indicatorColor = MovaColor.PrimaryOrangeLight,
                    unselectedIconColor = MovaColor.TextSecondary,
                    unselectedTextColor = MovaColor.TextSecondary
                )
            )
        }
    }
}


