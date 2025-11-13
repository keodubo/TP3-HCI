package com.comprartir.mobile.lists.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.comprartir.mobile.lists.data.ShoppingList
import com.comprartir.mobile.lists.data.ShoppingListsRepository
import com.comprartir.mobile.R
import com.comprartir.mobile.feature.lists.model.CreateListUiState
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
        android.util.Log.d(TAG, "init: ============================================")
        android.util.Log.d(TAG, "init: ListsViewModel initialized")
        android.util.Log.d(TAG, "init: ============================================")
        viewModelScope.launch {
            repository.observeLists().collectLatest { lists ->
                android.util.Log.d(TAG, "observeLists collectLatest: Received ${lists.size} lists from repository")
                android.util.Log.d(TAG, "observeLists collectLatest: List IDs = ${lists.map { it.id }}")
                android.util.Log.d(TAG, "observeLists collectLatest: List names = ${lists.map { it.name }}")
                _state.update { currentState ->
                    android.util.Log.d(TAG, "observeLists collectLatest: Updating UI state from ${currentState.lists.size} to ${lists.size} lists")
                    currentState.copy(lists = lists, isLoading = false)
                }
                android.util.Log.d(TAG, "observeLists collectLatest: UI state updated, new state has ${_state.value.lists.size} lists")
            }
        }
        android.util.Log.d(TAG, "init: Calling initial refresh()")
        refresh()
    }

    fun showCreateDialog() {
        android.util.Log.d(TAG, "showCreateDialog: Opening create list dialog")
        _state.update { 
            it.copy(
                createListState = CreateListUiState(isVisible = true)
            ) 
        }
    }

    fun dismissCreateDialog() {
        android.util.Log.d(TAG, "dismissCreateDialog: Closing create list dialog")
        _state.update { it.copy(createListState = CreateListUiState()) }
    }

    fun onCreateListNameChanged(name: String) {
        _state.update { 
            it.copy(
                createListState = it.createListState.copy(
                    name = name,
                    errorMessageRes = null
                )
            )
        }
    }

    fun onCreateListDescriptionChanged(description: String) {
        _state.update { 
            it.copy(
                createListState = it.createListState.copy(description = description)
            )
        }
    }

    fun onCreateListRecurringChanged(isRecurring: Boolean) {
        _state.update { 
            it.copy(
                createListState = it.createListState.copy(isRecurring = isRecurring)
            )
        }
    }

    fun confirmCreateList() {
        val currentState = state.value.createListState
        
        if (currentState.isSubmitting) {
            android.util.Log.w(TAG, "confirmCreateList: Already submitting, ignoring")
            return
        }
        
        if (currentState.name.isBlank()) {
            android.util.Log.w(TAG, "confirmCreateList: Name is blank")
            _state.update {
                it.copy(
                    createListState = it.createListState.copy(
                        errorMessageRes = R.string.lists_create_dialog_name_error
                    )
                )
            }
            return
        }

        android.util.Log.d(TAG, "confirmCreateList: Starting creation - name='${currentState.name}', description='${currentState.description}', recurring=${currentState.isRecurring}")
        
        _state.update { 
            it.copy(
                createListState = it.createListState.copy(
                    isSubmitting = true,
                    errorMessageRes = null
                )
            )
        }

        viewModelScope.launch {
            runCatching { 
                android.util.Log.d(TAG, "confirmCreateList: Calling repository.createList...")
                repository.createList(
                    name = currentState.name.trim(),
                    description = currentState.description.trim().ifBlank { null },
                    isRecurring = currentState.isRecurring
                )
                android.util.Log.d(TAG, "confirmCreateList: List created successfully!")
            }
                .onSuccess {
                    android.util.Log.d(TAG, "confirmCreateList: Success! Closing dialog and refreshing...")
                    dismissCreateDialog()
                    refresh()
                }
                .onFailure { throwable ->
                    android.util.Log.e(TAG, "confirmCreateList: FAILED - ${throwable.message}", throwable)
                    _state.update { current -> 
                        current.copy(
                            createListState = current.createListState.copy(
                                isSubmitting = false,
                                errorMessageRes = R.string.error_creating_list
                            ),
                            errorMessage = throwable.message
                        ) 
                    }
                }
        }
    }

    fun refresh() {
        android.util.Log.d(TAG, "refresh: Starting manual refresh...")
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching { 
                repository.refresh()
                android.util.Log.d(TAG, "refresh: Refresh completed successfully")
            }
                .onFailure { throwable ->
                    android.util.Log.e(TAG, "refresh: FAILED - ${throwable.message}", throwable)
                    _state.update { current -> current.copy(errorMessage = throwable.message) }
                }
            _state.update { it.copy(isLoading = false) }
        }
    }

    companion object {
        private const val TAG = "ListsViewModel"
    }

    fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }
}

data class ListsUiState(
    val lists: List<ShoppingList> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val createListState: CreateListUiState = CreateListUiState(),
)
