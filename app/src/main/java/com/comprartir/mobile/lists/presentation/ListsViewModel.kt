package com.comprartir.mobile.lists.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.comprartir.mobile.lists.data.ShoppingList
import com.comprartir.mobile.lists.data.ShoppingListsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ListsViewModel @Inject constructor(
    private val repository: ShoppingListsRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ListsUiState())
    val state: StateFlow<ListsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeLists().collectLatest { lists ->
                _state.update { it.copy(lists = lists) }
            }
        }
        refresh()
    }

    fun createList(name: String, description: String? = null) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching { repository.createList(name = name, description = description) }
                .onFailure { throwable ->
                    _state.update { current -> current.copy(errorMessage = throwable.message) }
                }
            _state.update { it.copy(isLoading = false) }
        }
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

data class ListsUiState(
    val lists: List<ShoppingList> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)
