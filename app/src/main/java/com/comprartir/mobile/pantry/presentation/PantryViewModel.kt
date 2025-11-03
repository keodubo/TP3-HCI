package com.comprartir.mobile.pantry.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.comprartir.mobile.pantry.data.PantryItem
import com.comprartir.mobile.pantry.data.PantryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class PantryViewModel @Inject constructor(
    private val repository: PantryRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(PantryUiState())
    val state: StateFlow<PantryUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observePantry().collectLatest { items ->
                _state.update { it.copy(items = items, isLoading = false) }
            }
        }
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching { repository.refresh() }
                .onFailure { throwable ->
                    _state.update { current -> current.copy(errorMessage = throwable.message) }
                }
            _state.update { it.copy(isLoading = false) }
        }
    }

    fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }
}

data class PantryUiState(
    val items: List<PantryItem> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)
