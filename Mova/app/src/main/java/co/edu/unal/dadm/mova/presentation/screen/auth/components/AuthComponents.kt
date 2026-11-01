package co.edu.unal.dadm.mova.presentation.screen.auth.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import co.edu.unal.dadm.mova.ui.theme.MovaBorder
import co.edu.unal.dadm.mova.ui.theme.MovaField
import co.edu.unal.dadm.mova.ui.theme.MovaInk
import co.edu.unal.dadm.mova.ui.theme.MovaOrange

@Composable
fun MovaLogo(modifier: Modifier = Modifier, inverse: Boolean = false) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier.size(76.dp).background(if (inverse) Color.White else MovaOrange, RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("M", color = if (inverse) MovaOrange else Color.White, style = MaterialTheme.typography.headlineLarge)
        }
        Text(
            "MOVA",
            color = if (inverse) Color.White else MovaInk,
            style = MaterialTheme.typography.titleLarge
        )
    }
}

@Composable
fun AuthHeader(title: String, subtitle: String, onBack: (() -> Unit)? = null) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        if (onBack != null) {
            IconButton(onClick = onBack, modifier = Modifier.size(34.dp)) { Text("‹") }
        }
        MovaLogo()
        Text(title, style = MaterialTheme.typography.headlineSmall)
        Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun AuthField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    isError: Boolean = false
) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth().height(58.dp),
            singleLine = true,
            visualTransformation = visualTransformation,
            isError = isError,
            shape = RoundedCornerShape(14.dp),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = MovaField,
                focusedContainerColor = Color.White,
                unfocusedIndicatorColor = MovaBorder,
                focusedIndicatorColor = MovaOrange
            )
        )
    }
}

@Composable
fun MovaButton(text: String, enabled: Boolean = true, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MovaInk)
    ) { Text(text) }
}

@Composable
fun SocialLoginRow(enabled: Boolean, onGoogleClick: () -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        IconButton(
            onClick = onGoogleClick,
            enabled = enabled,
            modifier = Modifier.size(48.dp).border(1.dp, MovaBorder, CircleShape)
        ) { Text("G") }
        IconButton(
            onClick = {},
            enabled = false,
            modifier = Modifier.size(48.dp).border(1.dp, MovaBorder, CircleShape)
        ) { Text("f") }
        IconButton(
            onClick = {},
            enabled = false,
            modifier = Modifier.size(48.dp).border(1.dp, MovaBorder, CircleShape)
        ) { Text("in") }
    }
}
