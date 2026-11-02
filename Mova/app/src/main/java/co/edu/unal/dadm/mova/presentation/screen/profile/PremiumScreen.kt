package co.edu.unal.dadm.mova.presentation.screen.profile

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import co.edu.unal.dadm.mova.presentation.components.MovaPrimaryButton
import co.edu.unal.dadm.mova.presentation.components.TopBarBack
import co.edu.unal.dadm.mova.presentation.theme.MovaMuted
import co.edu.unal.dadm.mova.presentation.theme.MovaOrange

@Composable
fun PremiumScreen(
    onPurchase: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedPlan by remember { mutableStateOf(1) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TopBarBack(onBack = onBack, foreground = MaterialTheme.colorScheme.onBackground)

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(MovaOrange),
                contentAlignment = Alignment.Center
            ) {
                Text("👑", fontSize = 38.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text("MOVA Premium", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Text("Unlock your full AI fitness potential", fontSize = 14.sp, color = MovaMuted)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Features list
        PremiumFeatureRow("🤖", "Unlimited AI Vocal & Chat Coaching")
        PremiumFeatureRow("⚡", "Hyper-Personalized Adaptive Routines")
        PremiumFeatureRow("📊", "Deep Health & Macro Analytics")
        PremiumFeatureRow("🔥", "Priority AI Scan Processing")

        Spacer(modifier = Modifier.height(12.dp))

        // Plan Selection
        PlanCard(
            title = "Yearly Pass",
            price = "$59.99 / year ($4.99/mo)",
            badge = "SAVE 50%",
            isSelected = selectedPlan == 1,
            onClick = { selectedPlan = 1 }
        )

        PlanCard(
            title = "Monthly Pass",
            price = "$9.99 / month",
            badge = null,
            isSelected = selectedPlan == 0,
            onClick = { selectedPlan = 0 }
        )

        Spacer(modifier = Modifier.height(16.dp))

        MovaPrimaryButton(
            text = "Subscribe Now",
            onClick = {
                onPurchase()
                onBack()
            },
            containerColor = MovaOrange,
            contentColor = Color.White
        )

        TextButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Restore Purchases", color = MovaMuted, fontSize = 13.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun PremiumFeatureRow(icon: String, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(icon, fontSize = 22.sp)
        Spacer(modifier = Modifier.width(12.dp))
        Text(text, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun PlanCard(title: String, price: String, badge: String?, isSelected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .then(if (isSelected) Modifier.border(2.dp, MovaOrange, RoundedCornerShape(20.dp)) else Modifier)
            .clickable(onClick = onClick)
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (badge != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MovaOrange)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(badge, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(price, fontSize = 13.sp, color = MovaMuted)
        }
    }
}
