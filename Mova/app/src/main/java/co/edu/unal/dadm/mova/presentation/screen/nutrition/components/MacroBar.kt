package co.edu.unal.dadm.mova.presentation.screen.nutrition.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun MacroBar(label: String, value: Double, goal: Double, color: Color, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            Text("${value.roundToInt()} / ${goal.roundToInt()} g", style = MaterialTheme.typography.bodyMedium)
        }
        LinearProgressIndicator(
            progress = { if (goal <= 0) 0f else (value / goal).toFloat().coerceIn(0f, 1f) },
            color = color,
            modifier = Modifier.fillMaxWidth().height(8.dp)
        )
    }
}
