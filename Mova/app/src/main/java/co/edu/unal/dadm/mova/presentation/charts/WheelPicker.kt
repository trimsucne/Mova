package co.edu.unal.dadm.mova.presentation.charts

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.edu.unal.dadm.mova.presentation.theme.MovaOrange
import kotlin.math.abs

@Composable
fun WheelPicker(
    selectedValue: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    range: IntRange = 13..120,
    visibleItemsCount: Int = 5
) {
    val items = remember(range) { range.toList() }
    val initialIndex = remember(selectedValue, items) { items.indexOf(selectedValue).coerceAtLeast(0) }
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    val itemHeight = 64.dp
    val height = itemHeight * visibleItemsCount

    val selectedIndex by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val visibleItems = layoutInfo.visibleItemsInfo
            if (visibleItems.isEmpty()) initialIndex
            else {
                val viewportCenter = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
                visibleItems.minByOrNull { abs((it.offset + it.size / 2) - viewportCenter) }?.index ?: initialIndex
            }
        }
    }

    LaunchedEffect(selectedIndex) {
        if (selectedIndex in items.indices && items[selectedIndex] != selectedValue) {
            onValueChange(items[selectedIndex])
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height),
        contentAlignment = Alignment.Center
    ) {
        LazyColumn(
            state = listState,
            flingBehavior = flingBehavior,
            contentPadding = PaddingValues(vertical = itemHeight * (visibleItemsCount / 2)),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(items.size) { index ->
                val item = items[index]
                val isSelected = index == selectedIndex
                val distance = abs(index - selectedIndex)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .height(56.dp)
                                .fillMaxWidth(0.42f)
                                .clip(RoundedCornerShape(18.dp))
                                .background(MovaOrange),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = item.toString(),
                                color = Color.White,
                                fontSize = 38.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Serif
                            )
                        }
                    } else {
                        val alpha = when (distance) {
                            1 -> 0.45f
                            2 -> 0.25f
                            else -> 0.15f
                        }
                        Text(
                            text = item.toString(),
                            color = Color.DarkGray.copy(alpha = alpha),
                            fontSize = if (distance == 1) 28.sp else 22.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = FontFamily.Serif,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
