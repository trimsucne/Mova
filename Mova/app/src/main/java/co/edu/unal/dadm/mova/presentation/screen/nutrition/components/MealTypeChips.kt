package co.edu.unal.dadm.mova.presentation.screen.nutrition.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import co.edu.unal.dadm.mova.domain.model.MealType

@Composable
fun MealTypeChips(
    selected: MealType?,
    onSelect: (MealType?) -> Unit,
    includeAll: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (includeAll) {
            FilterChip(selected = selected == null, onClick = { onSelect(null) }, label = { Text("All") })
        }
        MealType.entries.forEach { type ->
            FilterChip(selected = selected == type, onClick = { onSelect(type) }, label = { Text(type.label) })
        }
    }
}
