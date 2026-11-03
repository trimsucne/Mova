package co.edu.unal.dadm.mova.presentation.viewmodel.activity

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AddActivityViewModel : ViewModel() {
    private val _state = MutableStateFlow(AddActivityUiState())
    val state: StateFlow<AddActivityUiState> = _state.asStateFlow()

    fun onEvent(event: AddActivityEvent) {
        if (event is AddActivityEvent.TypeSelected) _state.update { it.copy(selected = event.type) }
        // Continue / Back los resuelve la Route (navegacion)
    }
}
