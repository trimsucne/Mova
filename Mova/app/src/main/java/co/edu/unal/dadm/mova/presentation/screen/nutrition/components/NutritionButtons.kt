package co.edu.unal.dadm.mova.presentation.screen.nutrition.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.edu.unal.dadm.mova.presentation.components.MovaPrimaryButton
import co.edu.unal.dadm.mova.presentation.theme.MovaField
import co.edu.unal.dadm.mova.presentation.theme.MovaInk
import co.edu.unal.dadm.mova.presentation.theme.MovaOrange

/** Botón de volver cuadrado redondeado (gris claro; translúcido blanco sobre fondos oscuros/naranja). */
@Composable
fun RoundedSquareBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onDark: Boolean = false,
    glyph: String = "‹"
) {
    Box(
        modifier = modifier
            .size(46.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (onDark) Color.White.copy(alpha = 0.2f) else MovaField)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = glyph,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = if (onDark) Color.White else MovaInk
        )
    }
}

/** Botón principal de las pantallas de nutrición: ancho completo, 56dp, radio 16dp, texto + " →". */
@Composable
fun PrimaryArrowButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor: Color = MovaOrange,
    trailingText: String? = "→"
) {
    MovaPrimaryButton(
        text = text,
        onClick = onClick,
        modifier = modifier.height(56.dp),
        enabled = enabled,
        containerColor = containerColor,
        trailingText = trailingText
    )
}
