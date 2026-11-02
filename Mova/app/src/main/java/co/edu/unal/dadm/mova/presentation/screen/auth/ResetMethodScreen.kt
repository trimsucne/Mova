package co.edu.unal.dadm.mova.presentation.screen.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.edu.unal.dadm.mova.R
import co.edu.unal.dadm.mova.presentation.components.ImagePlaceholder
import co.edu.unal.dadm.mova.presentation.components.MovaPrimaryButton
import co.edu.unal.dadm.mova.presentation.components.TopBarBack
import co.edu.unal.dadm.mova.presentation.theme.MovaBlueIcon
import co.edu.unal.dadm.mova.presentation.theme.MovaField
import co.edu.unal.dadm.mova.presentation.theme.MovaMuted
import co.edu.unal.dadm.mova.presentation.theme.MovaOrange
import co.edu.unal.dadm.mova.presentation.theme.MovaPurpleIcon

@Composable
fun ResetMethodScreen(
    onEmailSelected: () -> Unit,
    onGoogleSelected: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedMethod by remember { mutableStateOf(0) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TopBarBack(onBack = onBack, foreground = MaterialTheme.colorScheme.onBackground)

            Text(
                text = "Reset Password",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "Select what method you'd like to reset.",
                fontSize = 14.sp,
                color = MovaMuted
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Method 1: Email (Orange Icon Box)
            ResetMethodCard(
                icon = "✉",
                iconBg = MovaOrange,
                title = "Send via Email",
                description = "Seamlessly reset your password via email address.",
                isSelected = selectedMethod == 0,
                onClick = {
                    selectedMethod = 0
                    onEmailSelected()
                }
            )

            // Method 2: 2FA (Blue Icon Box)
            ResetMethodCard(
                icon = "🔒",
                iconBg = MovaBlueIcon,
                title = "Send via 2FA",
                description = "Seamlessly reset your password via 2 Factors.",
                isSelected = selectedMethod == 1,
                onClick = { selectedMethod = 1 }
            )

            // Method 3: Google Auth (Purple Icon Box)
            ResetMethodCard(
                icon = "🔥",
                iconBg = MovaPurpleIcon,
                title = "Send via Google Auth",
                description = "Seamlessly reset your password via gAuth.",
                isSelected = selectedMethod == 2,
                onClick = {
                    selectedMethod = 2
                    onGoogleSelected()
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            MovaPrimaryButton(
                text = "Reset Password",
                onClick = {
                    if (selectedMethod == 2) onGoogleSelected() else onEmailSelected()
                },
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Metallic padlock graphic
            ImagePlaceholder(
                label = "Metallic Padlock Security Graphic",
                drawableResId = R.drawable.img_metallic_lock,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
            )
        }
    }
}

@Composable
private fun ResetMethodCard(
    icon: String,
    iconBg: Color,
    title: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MovaField)
            .then(
                if (isSelected) Modifier.border(2.dp, MovaOrange, RoundedCornerShape(20.dp))
                else Modifier
            )
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = icon,
                fontSize = 24.sp,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                fontSize = 12.sp,
                color = MovaMuted,
                lineHeight = 16.sp
            )
        }

        Text(
            text = "›",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MovaMuted
        )
    }
}
