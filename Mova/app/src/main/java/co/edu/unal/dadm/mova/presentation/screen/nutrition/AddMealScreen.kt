package co.edu.unal.dadm.mova.presentation.screen.nutrition

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.unal.dadm.mova.core.di.NutritionContainer
import co.edu.unal.dadm.mova.domain.model.MealType
import co.edu.unal.dadm.mova.presentation.screen.nutrition.components.PrimaryArrowButton
import co.edu.unal.dadm.mova.presentation.screen.nutrition.components.LightStatusBarIcons
import co.edu.unal.dadm.mova.presentation.screen.nutrition.components.RoundedSquareBackButton
import co.edu.unal.dadm.mova.presentation.theme.MovaBlueIcon
import co.edu.unal.dadm.mova.presentation.theme.MovaField
import co.edu.unal.dadm.mova.presentation.theme.MovaInk
import co.edu.unal.dadm.mova.presentation.theme.MovaMuted
import co.edu.unal.dadm.mova.presentation.theme.MovaOrange
import co.edu.unal.dadm.mova.presentation.theme.MovaTheme
import co.edu.unal.dadm.mova.presentation.viewmodel.nutrition.AddMealEvent
import co.edu.unal.dadm.mova.presentation.viewmodel.nutrition.AddMealUiState
import co.edu.unal.dadm.mova.presentation.viewmodel.nutrition.AddMealViewModel
import co.edu.unal.dadm.mova.presentation.viewmodel.nutrition.NutritionViewModelFactory
import java.time.LocalDate

private val ScreenBg = Color(0xFFF8F8F8)

@Composable
fun AddMealScreen(
    container: NutritionContainer,
    date: LocalDate,
    initialType: MealType?,
    onBack: () -> Unit,
    onAdded: (LocalDate) -> Unit
) {
    val viewModel: AddMealViewModel = viewModel(factory = NutritionViewModelFactory {
        AddMealViewModel(date, LocalDate.now(), initialType, container.logMeal, container.calculateKcal, container.currentUserId)
    })
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.addedOn) { state.addedOn?.let(onAdded) }

    AddMealContent(
        state = state,
        onBack = onBack,
        onNameChanged = { viewModel.onEvent(AddMealEvent.NameChanged(it)) },
        onMealTypeChanged = { viewModel.onEvent(AddMealEvent.MealTypeChanged(it)) },
        onProteinChanged = { viewModel.onEvent(AddMealEvent.ProteinChanged(it)) },
        onCarbsChanged = { viewModel.onEvent(AddMealEvent.CarbsChanged(it)) },
        onFatChanged = { viewModel.onEvent(AddMealEvent.FatChanged(it)) },
        onSave = { viewModel.onEvent(AddMealEvent.Save) }
    )
}

@Composable
private fun AddMealContent(
    state: AddMealUiState,
    onBack: () -> Unit,
    onNameChanged: (String) -> Unit,
    onMealTypeChanged: (MealType) -> Unit,
    onProteinChanged: (String) -> Unit,
    onCarbsChanged: (String) -> Unit,
    onFatChanged: (String) -> Unit,
    onSave: () -> Unit
) {
    LightStatusBarIcons()
    Column(modifier = Modifier.fillMaxSize().background(ScreenBg).navigationBarsPadding()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                .background(MovaInk)
                .statusBarsPadding()
                .padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 28.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            RoundedSquareBackButton(onClick = onBack, onDark = true)
            Text("Add New Meal", fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
        }
        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SectionTitle("Meal Name")
            Box(
                modifier = Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(16.dp)).background(MovaField)
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🍴", fontSize = 18.sp)
                    Spacer(Modifier.width(12.dp))
                    Box(modifier = Modifier.weight(1f)) {
                        if (state.name.isEmpty()) {
                            Text("Enter your meal name...", color = MovaMuted, fontSize = 16.sp)
                        }
                        BasicTextField(
                            value = state.name,
                            onValueChange = onNameChanged,
                            singleLine = true,
                            textStyle = TextStyle(color = MovaInk, fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
                            cursorBrush = SolidColor(MovaOrange),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
            state.nameError?.let { ErrorText(it) }

            SectionTitle("Meal Type")
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()).padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MealType.entries.forEach { type ->
                    TypeChip(label = type.label, selected = state.mealType == type, onClick = { onMealTypeChanged(type) })
                }
            }

            MacroField("Total Protein", state.protein, onProteinChanged)
            MacroField("Total Carbs", state.carbs, onCarbsChanged)
            MacroField("Total Fat", state.fat, onFatChanged)
            state.macroError?.let { ErrorText(it) }
            Text("${state.kcal} kcal", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MovaMuted)
            state.error?.let { ErrorText(it) }
        }
        PrimaryArrowButton(
            text = if (state.isSaving) "Adding..." else "Continue",
            onClick = onSave,
            enabled = !state.isSaving,
            containerColor = MovaInk,
            modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, bottom = 20.dp, top = 8.dp)
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MovaInk)
}

@Composable
private fun ErrorText(text: String) {
    Text(text, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
}

@Composable
private fun TypeChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(50)
    Box(
        modifier = Modifier
            .then(if (selected) Modifier.border(3.dp, MovaBlueIcon.copy(alpha = 0.25f), shape).padding(3.dp) else Modifier.padding(3.dp))
            .clip(shape)
            .background(if (selected) MovaBlueIcon else MovaField)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(label, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = if (selected) Color.White else MovaInk)
            Box(
                modifier = Modifier.size(16.dp).clip(RoundedCornerShape(4.dp))
                    .border(1.5.dp, if (selected) Color.White else MovaMuted, RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (selected) Box(Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(Color.White))
            }
        }
    }
}

@Composable
private fun MacroField(label: String, value: String, onChange: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(label, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = MovaInk, modifier = Modifier.weight(1f))
            Text("gram", fontSize = 14.sp, color = MovaMuted)
        }
        Row(
            modifier = Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(16.dp)).background(MovaField),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(Modifier.width(14.dp))
            Box(modifier = Modifier.width(4.dp).height(28.dp).clip(RoundedCornerShape(2.dp)).background(MovaOrange))
            Spacer(Modifier.width(14.dp))
            Box(modifier = Modifier.weight(1f)) {
                if (value.isEmpty()) Text("0", color = MovaMuted, fontSize = 18.sp)
                BasicTextField(
                    value = value,
                    onValueChange = onChange,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    textStyle = TextStyle(color = MovaOrange, fontSize = 18.sp, fontWeight = FontWeight.Bold),
                    cursorBrush = SolidColor(MovaOrange),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun AddMealPreview() {
    MovaTheme {
        AddMealContent(
            state = AddMealUiState(name = "Avocado Toast", protein = "12", carbs = "30", fat = "15", mealType = MealType.LUNCH, kcal = 303),
            onBack = {}, onNameChanged = {}, onMealTypeChanged = {}, onProteinChanged = {}, onCarbsChanged = {}, onFatChanged = {}, onSave = {}
        )
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun AddMealEmptyErrorPreview() {
    MovaTheme {
        AddMealContent(
            state = AddMealUiState(nameError = "Enter a meal name", macroError = "Macros must be between 0 and 300 g"),
            onBack = {}, onNameChanged = {}, onMealTypeChanged = {}, onProteinChanged = {}, onCarbsChanged = {}, onFatChanged = {}, onSave = {}
        )
    }
}
