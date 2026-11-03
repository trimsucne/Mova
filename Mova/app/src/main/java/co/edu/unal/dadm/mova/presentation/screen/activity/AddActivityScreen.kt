package co.edu.unal.dadm.mova.presentation.screen.activity

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.unal.dadm.mova.domain.model.ActivityType
import co.edu.unal.dadm.mova.presentation.components.MovaPrimaryButton
import co.edu.unal.dadm.mova.presentation.components.TopBarBack
import co.edu.unal.dadm.mova.presentation.theme.MovaOrange
import co.edu.unal.dadm.mova.presentation.theme.MovaTheme
import co.edu.unal.dadm.mova.presentation.viewmodel.activity.AddActivityEvent
import co.edu.unal.dadm.mova.presentation.viewmodel.activity.AddActivityUiState
import co.edu.unal.dadm.mova.presentation.viewmodel.activity.AddActivityViewModel

@Composable
fun AddActivityRoute(onContinue: (ActivityType) -> Unit, onBack: () -> Unit) {
    val viewModel: AddActivityViewModel = viewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    AddActivityScreen(state) { event ->
        when (event) {
            AddActivityEvent.ContinueClicked -> state.selected?.let(onContinue)
            AddActivityEvent.BackClicked -> onBack()
            else -> viewModel.onEvent(event)
        }
    }
}

@Composable
fun AddActivityScreen(state: AddActivityUiState, onEvent: (AddActivityEvent) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TopBarBack(onBack = { onEvent(AddActivityEvent.BackClicked) })
        Text("Add New Activity", fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 16.dp))
        Text("Please select activity type", fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 8.dp, bottom = 24.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(state.types, key = { it.id }) { type ->
                val selected = type == state.selected
                Column(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(24.dp))
                        .background(if (selected) MovaOrange else MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { onEvent(AddActivityEvent.TypeSelected(type)) }
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(type.emoji(), fontSize = 32.sp)
                    Text(
                        type.label, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1,
                        color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }
        }
        MovaPrimaryButton(
            text = "Continue",
            onClick = { onEvent(AddActivityEvent.ContinueClicked) },
            enabled = state.canContinue,
            containerColor = MovaOrange,
            trailingText = null,
            modifier = Modifier.padding(vertical = 16.dp)
        )
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun AddActivityPreview() {
    MovaTheme(darkTheme = false) { AddActivityScreen(AddActivityUiState(selected = ActivityType.Jogging), {}) }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun AddActivityDarkPreview() {
    MovaTheme(darkTheme = true) { AddActivityScreen(AddActivityUiState(), {}) }
}
