package co.edu.unal.dadm.mova.presentation.screen.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.edu.unal.dadm.mova.presentation.theme.MovaColor

@Composable
fun CoachCard(
    onAskCoachClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = MovaColor.PrimaryOrangeLight)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Virtual AI Coach 🤖",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MovaColor.PrimaryOrangeDark
                )
                Text(
                    text = "Get instant personalized fitness & meal suggestions.",
                    fontSize = 12.sp,
                    color = MovaColor.TextDarkGray
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onAskCoachClick,
                colors = ButtonDefaults.buttonColors(containerColor = MovaColor.PrimaryOrange),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Chat", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

