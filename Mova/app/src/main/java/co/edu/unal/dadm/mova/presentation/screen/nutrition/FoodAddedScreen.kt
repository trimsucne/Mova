package co.edu.unal.dadm.mova.presentation.screen.nutrition

import androidx.compose.runtime.Composable
import co.edu.unal.dadm.mova.presentation.components.SuccessDialog

@Composable
fun FoodAddedScreen(onBackToMyMeals: () -> Unit) {
    SuccessDialog(
        title = "Food Added!",
        message = "Your meal was added to My Meals.",
        buttonText = "Back to My Meals",
        onButtonClick = onBackToMyMeals,
        onClose = onBackToMyMeals
    )
}
