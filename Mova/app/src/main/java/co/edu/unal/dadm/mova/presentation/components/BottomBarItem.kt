package co.edu.unal.dadm.mova.presentation.components

import androidx.compose.ui.graphics.vector.ImageVector

data class BottomBarItem(
    val route: String,
    val title: String,
    val icon: ImageVector,
    val badgeCount: Int = 0
)

