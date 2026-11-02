package co.edu.unal.dadm.mova.presentation.screen.nutrition.components

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import co.edu.unal.dadm.mova.presentation.components.ImagePlaceholder

/** Imagen local del catálogo (res/drawable/{imageRes}); si aún no existe, muestra el nombre. */
@SuppressLint("DiscouragedApi")
@Composable
fun MealImage(name: String, imageRes: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val drawableId = remember(imageRes) {
        context.resources.getIdentifier(imageRes, "drawable", context.packageName).takeIf { it != 0 }
    }
    ImagePlaceholder(label = name, modifier = modifier, drawableResId = drawableId)
}
