package co.edu.unal.dadm.mova.presentation.screen.onboarding

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.edu.unal.dadm.mova.R
import co.edu.unal.dadm.mova.presentation.components.ImagePlaceholder
import co.edu.unal.dadm.mova.presentation.theme.MovaField
import co.edu.unal.dadm.mova.presentation.theme.MovaInk
import co.edu.unal.dadm.mova.presentation.theme.MovaMuted
import co.edu.unal.dadm.mova.presentation.theme.MovaOrange

@Composable
fun AvatarStep(
    selected: String?,
    onSelected: (String) -> Unit,
    onContinue: () -> Unit,
    onBack: (() -> Unit)? = null
) {
    val avatars = listOf("avatar_1", "avatar_2", "avatar_3")
    val activeAvatar = selected ?: "avatar_2"

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onSelected(it.toString()) }
    }

    AssessmentStep(
        stepIndex = 8,
        totalSteps = 9,
        title = "Select your Avatar",
        description = "We have 23 custom premade avatars, or you can upload profile locally",
        canContinue = true,
        onContinue = onContinue,
        onBack = onBack
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 3D Avatar Row Carousel
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                avatars.forEach { avatar ->
                    val isSelected = avatar == activeAvatar
                    val size = if (isSelected) 120.dp else 90.dp
                    val drawableRes = when (avatar) {
                        "avatar_1" -> R.drawable.img_avatar_1
                        "avatar_2" -> R.drawable.img_avatar_2
                        "avatar_3" -> R.drawable.img_avatar_3
                        else -> null
                    }

                    Box(
                        modifier = Modifier
                            .size(size)
                            .clip(RoundedCornerShape(32.dp))
                            .background(if (isSelected) MovaOrange else MovaField)
                            .clickable { onSelected(avatar) },
                        contentAlignment = Alignment.Center
                    ) {
                        ImagePlaceholder(
                            label = "3D Avatar $avatar",
                            modifier = Modifier.size(size),
                            drawableResId = drawableRes,
                            imagePath = if (drawableRes == null) avatar else null,
                            shape = RoundedCornerShape(32.dp),
                            backgroundColor = if (isSelected) MovaOrange else MovaField,
                            contentColor = if (isSelected) Color.White else MovaInk
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Local Upload Preview / Upload Button
            val isCustomUpload = activeAvatar.startsWith("content://") || activeAvatar.startsWith("/") || activeAvatar.startsWith("file://")

            if (isCustomUpload) {
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .border(3.dp, MovaOrange, CircleShape)
                        .clickable { imagePickerLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    ImagePlaceholder(
                        label = "Custom Upload Avatar",
                        imagePath = activeAvatar,
                        modifier = Modifier.size(110.dp),
                        shape = CircleShape
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .border(2.dp, MovaMuted, CircleShape)
                        .background(MovaField.copy(alpha = 0.5f))
                        .clickable { imagePickerLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "⇡",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = MovaInk
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = if (isCustomUpload) "Custom Avatar Selected (Tap to Change)" else "or Upload from Local File",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MovaInk,
                textAlign = TextAlign.Center,
                modifier = Modifier.clickable { imagePickerLauncher.launch("image/*") }
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Max 5mb, Format: jpg,png",
                fontSize = 13.sp,
                color = MovaMuted,
                textAlign = TextAlign.Center
            )
        }
    }
}
