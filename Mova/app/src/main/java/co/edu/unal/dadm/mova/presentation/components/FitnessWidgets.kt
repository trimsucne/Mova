package co.edu.unal.dadm.mova.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Valor grande + etiqueta (Time / Calorie / Sets). */
@Composable
fun StatColumn(value: String, label: String, modifier: Modifier = Modifier, color: Color = Color.White) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(value, color = color, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(label, color = color.copy(alpha = 0.7f), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}

/** Pastilla con borde (p. ej. "25 Total"). */
@Composable
fun OutlinePill(text: String, modifier: Modifier = Modifier, color: Color = Color.White) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Color.Transparent,
        border = BorderStroke(1.dp, color.copy(alpha = 0.7f))
    ) {
        Text(
            text,
            color = color,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        )
    }
}

fun formatClock(totalSec: Int): String = String.format("%02d:%02d", totalSec / 60, totalSec % 60)
