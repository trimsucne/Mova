package co.edu.unal.dadm.mova.presentation.viewmodel.nutrition

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/** Crea cualquier ViewModel del módulo a partir de una lambda (sin Hilt). */
class NutritionViewModelFactory<VM : ViewModel>(private val creator: () -> VM) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = creator() as T
}
