package co.edu.unal.dadm.mova.presentation.screen.nutrition

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.edu.unal.dadm.mova.presentation.screen.nutrition.components.PrimaryArrowButton
import co.edu.unal.dadm.mova.presentation.theme.MovaInk
import co.edu.unal.dadm.mova.presentation.theme.MovaMuted
import co.edu.unal.dadm.mova.presentation.theme.MovaSuccessGreenBg

@Composable
fun FoodAddedScreen(onBackToMyMeals: () -> Unit) {
    FoodAddedContent(onBackToMyMeals)
}

// SuccessDialog usa un scrim negro plano y un check circular; el mockup pide fondo oscuro con
// degradado, check en cuadrado redondeado y botón «Great, thanks! ✓», por eso se compone aquí.
@Composable
private fun FoodAddedContent(onBackToMyMeals: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF2A2B30), MovaInk)))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.size(64.dp).clip(RoundedCornerShape(18.dp)).background(MovaSuccessGreenBg),
                    contentAlignment = Alignment.Center
                ) {
                    Text("✓", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                }
                Spacer(Modifier.height(18.dp))
                Text("Food Added!", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MovaInk, textAlign = TextAlign.Center)
                Spacer(Modifier.height(10.dp))
                Text(
                    "You have successfully added a meal to your diet schedule",
                    fontSize = 14.sp, lineHeight = 20.sp, color = MovaMuted, textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(22.dp))
                PrimaryArrowButton(
                    text = "Great, thanks!",
                    onClick = onBackToMyMeals,
                    containerColor = MovaInk,
                    trailingText = "✓"
                )
            }
            Spacer(Modifier.height(24.dp))
            Box(
                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(14.dp)).background(Color.White).clickable(onClick = onBackToMyMeals),
                contentAlignment = Alignment.Center
            ) {
                Text("✕", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MovaInk)
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 780)
@Composable
private fun FoodAddedPreview() {
    FoodAddedContent(onBackToMyMeals = {})
}
