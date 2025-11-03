package com.comprartir.mobile.lists.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.comprartir.mobile.lists.data.ListItem
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
class ListDetailsViewModel @Inject constructor(
    private val repository: ShoppingListsRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val listId: String = savedStateHandle.get<String>("listId") ?: ""

    private val _state = MutableStateFlow(ListDetailsUiState(listId = listId))
    val state: StateFlow<ListDetailsUiState> = _state.asStateFlow()

    init {
        if (listId.isNotBlank()) {
            viewModelScope.launch {
                repository.observeList(listId).collectLatest { list ->
                    if (list != null) {
                        _state.update {
                            it.copy(
                                title = list.name,
                                items = list.items.map { item -> item.toUiModel() },
                                isLoading = false,
                            )
                        }
                    }
                }
            }
            refresh()
        } else {
            _state.update { it.copy(isLoading = false, errorMessage = "List not found") }
        }
    }

    fun toggleAcquired(itemId: String, acquired: Boolean) {
        if (listId.isBlank()) return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching { repository.toggleAcquired(listId, itemId, acquired) }
                .onFailure { throwable ->
                    _state.update { current -> current.copy(errorMessage = throwable.message) }
                }
            _state.update { it.copy(isLoading = false) }
        }
    }

    fun refresh() {
        if (listId.isBlank()) return
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

    private fun ListItem.toUiModel(): ListItemUiModel = ListItemUiModel(
        id = id,
        name = name,
        quantity = quantity,
        unit = unit,
        isAcquired = isAcquired,
        notes = notes,
    )
}

data class ListDetailsUiState(
    val listId: String,
    val title: String = "",
    val items: List<ListItemUiModel> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)

data class ListItemUiModel(
    val id: String,
    val name: String,
    val quantity: Double,
    val unit: String?,
    val isAcquired: Boolean,
    val notes: String?,
)
