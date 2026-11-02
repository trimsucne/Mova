package co.edu.unal.dadm.mova.presentation.screen.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.unal.dadm.mova.R
import co.edu.unal.dadm.mova.presentation.components.ImagePlaceholder
import co.edu.unal.dadm.mova.presentation.components.MovaPrimaryButton
import co.edu.unal.dadm.mova.presentation.screen.auth.components.MovaLogo
import co.edu.unal.dadm.mova.presentation.theme.MovaOrange
import co.edu.unal.dadm.mova.presentation.viewmodel.onboarding.IntroPagerViewModel

private val introPages = listOf(
    "Move with a plan" to "Build flexible routines around your real day.",
    "Track healthy habits" to "Keep progress visible across activity, food, water, and rest.",
    "Adapt and continue" to "A difficult day changes your plan; it does not end your progress."
)

@Composable
fun IntroPagerScreen(onFinished: () -> Unit) {
    val viewModel: IntroPagerViewModel = viewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val page = state.page
    val (title, body) = introPages[page]

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Gym Background Image
        ImagePlaceholder(
            label = "Gym Machinery Onboarding Background Image",
            drawableResId = R.drawable.img_welcome_bg,
            modifier = Modifier.fillMaxSize(),
            backgroundColor = MovaOrange
        )

        // Gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.25f),
                            Color.Black.copy(alpha = 0.75f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(28.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MovaLogo(inverse = true)

            Spacer(modifier = Modifier.height(24.dp))

            LinearProgressIndicator(
                progress = { (page + 1).toFloat() / state.pageCount },
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = MovaOrange,
                trackColor = Color.White.copy(alpha = 0.3f)
            )

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = title,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = body,
                fontSize = 15.sp,
                color = Color.White.copy(alpha = 0.85f),
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            MovaPrimaryButton(
                text = if (page == introPages.lastIndex) "Get Started" else "Next",
                onClick = {
                    if (state.isLastPage) onFinished() else viewModel.next()
                },
                containerColor = MovaOrange,
                contentColor = Color.White
            )
        }
    }
}
