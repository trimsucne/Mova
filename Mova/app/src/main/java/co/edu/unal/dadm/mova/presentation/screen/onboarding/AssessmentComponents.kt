package co.edu.unal.dadm.mova.presentation.screen.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import co.edu.unal.dadm.mova.ui.theme.MovaField
import co.edu.unal.dadm.mova.ui.theme.MovaInk
import co.edu.unal.dadm.mova.ui.theme.MovaOrange

@Composable
internal fun AssessmentStep(
    index: Int,
    title: String,
    description: String = "",
    canContinue: Boolean,
    onContinue: () -> Unit,
    accent: Boolean = false,
    content: @Composable () -> Unit
) {
    val foreground = if (accent) Color.White else MovaInk
    Column(
        Modifier.fillMaxSize().background(if (accent) MovaOrange else Color.White)
            .padding(horizontal = 10.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        AssessmentTopBar(index, foreground)
        Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = foreground)
        if (description.isNotBlank()) Text(description, color = foreground.copy(alpha = .7f))
        content()
        Spacer(Modifier.weight(1f))
        Button(
            onClick = onContinue,
            enabled = canContinue,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (accent) Color.White else MovaInk,
                contentColor = if (accent) MovaOrange else Color.White,
                disabledContainerColor = MovaInk.copy(alpha = .35f)
            )
        ) { Text("Continue   →") }
    }
}

@Composable
internal fun AssessmentTopBar(index: Int, foreground: Color = MovaInk, label: String = "Assessment") {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.background(MovaField, RoundedCornerShape(13.dp)).padding(horizontal = 12.dp, vertical = 7.dp)
        ) { Text("‹", color = foreground, style = MaterialTheme.typography.titleLarge) }
        Text(label, Modifier.padding(start = 9.dp), fontWeight = FontWeight.Bold, color = foreground)
        Spacer(Modifier.weight(1f))
        Text(
            "$index of 6",
            Modifier.background(Color(0xFFDCEBFF), RoundedCornerShape(10.dp)).padding(horizontal = 10.dp, vertical = 5.dp),
            color = Color(0xFF1769E0),
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
internal fun OptionButton(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth()
            .background(if (selected) MovaOrange else MovaField, RoundedCornerShape(12.dp))
            .then(if (selected) Modifier.border(2.dp, MovaOrange, RoundedCornerShape(12.dp)) else Modifier)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(if (selected) "◉" else "○", color = if (selected) Color.White else MovaInk)
        Text(label, Modifier.padding(start = 10.dp), color = if (selected) Color.White else MovaInk)
    }
}
