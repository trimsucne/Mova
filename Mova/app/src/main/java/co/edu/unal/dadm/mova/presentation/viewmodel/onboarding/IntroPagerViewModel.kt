package co.edu.unal.dadm.mova.presentation.viewmodel.onboarding

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class IntroPagerUiState(
    val page: Int = 0,
    val pageCount: Int = 3
) {
    val isLastPage: Boolean get() = page == pageCount - 1
}

class IntroPagerViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(IntroPagerUiState())
    val uiState: StateFlow<IntroPagerUiState> = _uiState.asStateFlow()

    fun next() {
        _uiState.value = _uiState.value.copy(
            page = (_uiState.value.page + 1).coerceAtMost(_uiState.value.pageCount - 1)
        )
    }
}
