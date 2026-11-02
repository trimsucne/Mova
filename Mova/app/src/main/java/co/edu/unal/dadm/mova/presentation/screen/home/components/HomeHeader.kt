package co.edu.unal.dadm.mova.presentation.screen.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.edu.unal.dadm.mova.presentation.components.MovaIcons
import co.edu.unal.dadm.mova.presentation.theme.MovaColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeHeader(
    userName: String,
    avatarUrl: String?,
    unreadNotificationsCount: Int,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearchSubmitted: (String) -> Unit,
    onNotificationsClick: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Hello, $userName 👋",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MovaColor.TextBlack
                )
                Text(
                    text = "Let's achieve your goals today!",
                    fontSize = 14.sp,
                    color = MovaColor.TextSecondary
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = onNotificationsClick) {
                    BadgedBox(
                        badge = {
                            if (unreadNotificationsCount > 0) {
                                Badge(containerColor = MovaColor.PrimaryOrange) {
                                    Text(unreadNotificationsCount.toString(), color = Color.White)
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = MovaIcons.Bell,
                            contentDescription = "Notifications",
                            tint = MovaColor.TextBlack
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MovaColor.PrimaryOrangeLight)
                        .clickable { onProfileClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = MovaIcons.Person,
                        contentDescription = "Profile",
                        tint = MovaColor.PrimaryOrange
                    )
                }
            }
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp)),
            placeholder = { Text("Search workouts, meals, activities...") },
            leadingIcon = {
                Icon(
                    imageVector = MovaIcons.Search,
                    contentDescription = "Search",
                    tint = MovaColor.TextSecondary
                )
            },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MovaColor.CardLightGray,
                unfocusedContainerColor = MovaColor.CardLightGray,
                focusedBorderColor = MovaColor.PrimaryOrange,
                unfocusedBorderColor = MovaColor.CardBorder
            ),
            shape = RoundedCornerShape(16.dp)
        )
    }
}


