package co.edu.unal.dadm.mova.presentation.screen.auth.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import co.edu.unal.dadm.mova.R
import co.edu.unal.dadm.mova.presentation.theme.MovaBorder

@Composable
fun SocialLoginRow(
    enabled: Boolean,
    onGoogleClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Instagram
        IconButton(
            onClick = {},
            enabled = enabled,
            modifier = Modifier
                .size(54.dp)
                .border(1.5.dp, MovaBorder, CircleShape)
                .padding(10.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_instagram),
                contentDescription = "Instagram Sign In",
                modifier = Modifier.size(30.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Google (Functional Sign-in)
        IconButton(
            onClick = onGoogleClick,
            enabled = enabled,
            modifier = Modifier
                .size(54.dp)
                .border(1.5.dp, MovaBorder, CircleShape)
                .padding(10.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_google),
                contentDescription = "Google Sign In",
                modifier = Modifier.size(30.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // LinkedIn
        IconButton(
            onClick = {},
            enabled = enabled,
            modifier = Modifier
                .size(54.dp)
                .border(1.5.dp, MovaBorder, CircleShape)
                .padding(10.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_linkedin),
                contentDescription = "LinkedIn Sign In",
                modifier = Modifier.size(30.dp)
            )
        }
    }
}
