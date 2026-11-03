package co.edu.unal.dadm.mova.presentation.screen.activity

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.edu.unal.dadm.mova.R
import co.edu.unal.dadm.mova.presentation.components.MovaPrimaryButton
import co.edu.unal.dadm.mova.presentation.components.TopBarBack
import co.edu.unal.dadm.mova.presentation.theme.MovaOrange
import co.edu.unal.dadm.mova.presentation.theme.MovaTheme

/** Estado vacio (mockup "No Activities"): foto en escala de grises + CTA naranja. */
@Composable
fun NoActivitiesScreen(onAddActivity: () -> Unit, onBack: () -> Unit, modifier: Modifier = Modifier) {
    val bg = MaterialThemeBackground()
    Box(modifier = modifier.fillMaxSize().background(bg)) {
        Image(
            painter = painterResource(R.drawable.img_male_runner),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            colorFilter = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) }),
            modifier = Modifier.fillMaxWidth().fillMaxSize(0.8f)
        )
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(0.45f to Color.Transparent, 0.75f to bg)
            )
        )
        TopBarBack(onBack = onBack, modifier = Modifier.statusBarsPadding().padding(horizontal = 20.dp).align(Alignment.TopStart))
        Column(
            modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("No Activities", fontSize = 30.sp, fontWeight = FontWeight.ExtraBold)
            Text(
                "You have 0 activities. Create a new one and start tracking your fitness activity.",
                textAlign = TextAlign.Center,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(vertical = 12.dp)
            )
            MovaPrimaryButton("Add New Activity", onAddActivity, containerColor = MovaOrange)
        }
    }
}

@Composable
private fun MaterialThemeBackground(): Color = androidx.compose.material3.MaterialTheme.colorScheme.background

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun NoActivitiesPreview() {
    MovaTheme(darkTheme = false) { NoActivitiesScreen({}, {}) }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun NoActivitiesDarkPreview() {
    MovaTheme(darkTheme = true) { NoActivitiesScreen({}, {}) }
}
