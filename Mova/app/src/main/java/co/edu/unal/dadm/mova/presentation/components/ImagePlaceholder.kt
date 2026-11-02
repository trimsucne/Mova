package co.edu.unal.dadm.mova.presentation.components

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.File

@Composable
fun ImagePlaceholder(
    label: String,
    modifier: Modifier = Modifier,
    @DrawableRes drawableResId: Int? = null,
    imagePath: String? = null,
    shape: Shape = RoundedCornerShape(14.dp),
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    contentScale: ContentScale = ContentScale.Crop
) {
    val context = LocalContext.current

    val localBitmap = remember(imagePath) {
        if (!imagePath.isNullOrEmpty()) {
            when {
                imagePath.startsWith("content://") -> {
                    runCatching {
                        context.contentResolver.openInputStream(Uri.parse(imagePath))?.use { stream ->
                            BitmapFactory.decodeStream(stream)?.asImageBitmap()
                        }
                    }.getOrNull()
                }
                imagePath.startsWith("/") || imagePath.startsWith("file://") -> {
                    val cleanPath = imagePath.removePrefix("file://")
                    val file = File(cleanPath)
                    if (file.exists()) {
                        runCatching {
                            BitmapFactory.decodeFile(file.absolutePath)?.asImageBitmap()
                        }.getOrNull()
                    } else null
                }
                else -> null
            }
        } else null
    }

    Box(
        modifier = modifier
            .clip(shape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        when {
            localBitmap != null -> {
                Image(
                    bitmap = localBitmap,
                    contentDescription = label,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = contentScale
                )
            }
            drawableResId != null -> {
                Image(
                    painter = painterResource(id = drawableResId),
                    contentDescription = label,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = contentScale
                )
            }
            else -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = "🖼️",
                        fontSize = 24.sp
                    )
                    Text(
                        text = label,
                        color = contentColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
