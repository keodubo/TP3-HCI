package com.comprartir.mobile.lists.presentation

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.comprartir.mobile.lists.data.ShoppingList
import com.comprartir.mobile.lists.data.ShoppingListsRepository
import com.comprartir.mobile.R
import com.comprartir.mobile.feature.lists.model.CreateListUiState
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
class ListsViewModel @Inject constructor(
    private val repository: ShoppingListsRepository,
    private val pantryRepository: PantryRepository,
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
        viewModelScope.launch {
            pantryRepository.observePantries().collectLatest { pantries ->
                val options = pantries.map { PantryOption(id = it.id, name = it.name) }
                _state.update { current ->
                    val updatedComplete = if (current.completeListState.isVisible) {
                        val selected = current.completeListState.selectedPantryId?.takeIf { id ->
                            options.any { it.id == id }
                        } ?: options.firstOrNull()?.id
                        current.completeListState.copy(
                            pantryOptions = options,
                            selectedPantryId = selected,
                        )
                    } else {
                        current.completeListState
                    }
                    current.copy(
                        pantryOptions = options,
                        completeListState = updatedComplete,
                    )
                }
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

    fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }

    // Edit list functions
    fun showEditDialog(list: ShoppingList) {
        android.util.Log.d(TAG, "showEditDialog: Opening edit dialog for list ${list.id} - '${list.name}'")
        _state.update { 
            it.copy(
                editListState = EditListUiState(
                    isVisible = true,
                    listId = list.id,
                    name = list.name,
                    description = list.description.orEmpty(),
                    isRecurring = list.isRecurring
                )
            ) 
        }
    }

    fun dismissEditDialog() {
        android.util.Log.d(TAG, "dismissEditDialog: Closing edit dialog")
        _state.update { it.copy(editListState = EditListUiState()) }
    }

    fun onEditListNameChanged(name: String) {
        _state.update { 
            it.copy(
                editListState = it.editListState.copy(
                    name = name,
                    errorMessageRes = null
                )
            )
        }
    }

    fun onEditListDescriptionChanged(description: String) {
        _state.update { 
            it.copy(
                editListState = it.editListState.copy(description = description)
            )
        }
    }

    fun onEditListRecurringChanged(isRecurring: Boolean) {
        _state.update { 
            it.copy(
                editListState = it.editListState.copy(isRecurring = isRecurring)
            )
        }
    }

    fun confirmEditList() {
        val currentState = state.value.editListState
        
        if (currentState.isSubmitting) {
            android.util.Log.w(TAG, "confirmEditList: Already submitting, ignoring")
            return
        }
        
        if (currentState.name.isBlank()) {
            android.util.Log.w(TAG, "confirmEditList: Name is blank")
            _state.update {
                it.copy(
                    editListState = it.editListState.copy(
                        errorMessageRes = R.string.lists_create_dialog_name_error
                    )
                )
            }
            return
        }

        android.util.Log.d(TAG, "confirmEditList: Starting update - listId=${currentState.listId}, name='${currentState.name}'")
        
        _state.update { 
            it.copy(
                editListState = it.editListState.copy(
                    isSubmitting = true,
                    errorMessageRes = null
                )
            )
        }

        viewModelScope.launch {
            runCatching { 
                android.util.Log.d(TAG, "confirmEditList: Calling repository.updateList...")
                repository.updateList(
                    listId = currentState.listId,
                    name = currentState.name.trim(),
                    description = currentState.description.trim().ifBlank { null }
                )
                android.util.Log.d(TAG, "confirmEditList: List updated successfully!")
            }
                .onSuccess {
                    android.util.Log.d(TAG, "confirmEditList: Success! Closing dialog and refreshing...")
                    dismissEditDialog()
                    refresh()
                }
                .onFailure { throwable ->
                    android.util.Log.e(TAG, "confirmEditList: FAILED - ${throwable.message}", throwable)
                    _state.update { current -> 
                        current.copy(
                            editListState = current.editListState.copy(
                                isSubmitting = false,
                                errorMessageRes = R.string.error_updating_list
                            ),
                            errorMessage = throwable.message
                        ) 
                    }
                }
        }
    }

    // Delete list functions
    fun showDeleteDialog(list: ShoppingList) {
        android.util.Log.d(TAG, "showDeleteDialog: Opening delete dialog for list ${list.id} - '${list.name}'")
        _state.update { 
            it.copy(
                deleteListState = DeleteListUiState(
                    isVisible = true,
                    listId = list.id,
                    listName = list.name
                )
            ) 
        }
    }

    fun dismissDeleteDialog() {
        android.util.Log.d(TAG, "dismissDeleteDialog: Closing delete dialog")
        _state.update { it.copy(deleteListState = DeleteListUiState()) }
    }

    fun confirmDeleteList() {
        val currentState = state.value.deleteListState
        
        if (currentState.isDeleting) {
            android.util.Log.w(TAG, "confirmDeleteList: Already deleting, ignoring")
            return
        }

        android.util.Log.d(TAG, "confirmDeleteList: Starting deletion - listId=${currentState.listId}")
        
        _state.update { 
            it.copy(
                deleteListState = it.deleteListState.copy(isDeleting = true)
            )
        }

        viewModelScope.launch {
            runCatching { 
                android.util.Log.d(TAG, "confirmDeleteList: Calling repository.deleteList...")
                repository.deleteList(currentState.listId)
                android.util.Log.d(TAG, "confirmDeleteList: List deleted successfully!")
            }
                .onSuccess {
                    android.util.Log.d(TAG, "confirmDeleteList: Success! Closing dialog and refreshing...")
                    dismissDeleteDialog()
                    refresh()
                }
                .onFailure { throwable ->
                    android.util.Log.e(TAG, "confirmDeleteList: FAILED - ${throwable.message}", throwable)
                    _state.update { current -> 
                        current.copy(
                            deleteListState = current.deleteListState.copy(isDeleting = false),
                            errorMessage = "No se pudo eliminar la lista: ${throwable.message}"
                        ) 
                    }
                }
        }
    }

    fun showCompleteDialog(list: ShoppingList) {
        val options = _state.value.pantryOptions
        _state.update {
            it.copy(
                completeListState = CompleteListUiState(
                    isVisible = true,
                    listId = list.id,
                    listName = list.name.ifBlank { list.id },
                    pantryOptions = options,
                    selectedPantryId = options.firstOrNull()?.id,
                )
            )
        }
    }

    fun dismissCompleteDialog() {
        _state.update { it.copy(completeListState = CompleteListUiState()) }
    }

    fun onCompletePantrySelected(pantryId: String) {
        _state.update {
            it.copy(
                completeListState = it.completeListState.copy(
                    selectedPantryId = pantryId,
                    errorMessageRes = null,
                )
            )
        }
    }

    fun confirmCompleteList() {
        val currentState = _state.value.completeListState
        if (!currentState.isVisible || currentState.isSubmitting) return
        val pantryId = currentState.selectedPantryId
        if (pantryId.isNullOrBlank()) {
            _state.update {
                it.copy(
                    completeListState = currentState.copy(
                        errorMessageRes = R.string.lists_complete_select_pantry_error
                    )
                )
            }
            return
        }

        _state.update {
            it.copy(
                completeListState = currentState.copy(
                    isSubmitting = true,
                    errorMessageRes = null,
                )
            )
        }

        val submittingState = _state.value.completeListState
        viewModelScope.launch {
            runCatching {
                repository.purchaseList(currentState.listId)
                repository.moveListToPantry(currentState.listId, pantryId)
                repository.resetList(currentState.listId)
                pantryRepository.refresh()
                repository.refresh()
            }.onSuccess {
                _state.update { it.copy(completeListState = CompleteListUiState()) }
            }.onFailure { throwable ->
                _state.update {
                    it.copy(
                        completeListState = submittingState.copy(
                            isSubmitting = false,
                            errorMessageRes = R.string.lists_complete_error,
                        ),
                        errorMessage = throwable.message,
                    )
                }
            }
        }
    }

    companion object {
        private const val TAG = "ListsViewModel"
    }
}

data class ListsUiState(
    val lists: List<ShoppingList> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val createListState: CreateListUiState = CreateListUiState(),
    val editListState: EditListUiState = EditListUiState(),
    val deleteListState: DeleteListUiState = DeleteListUiState(),
    val pantryOptions: List<PantryOption> = emptyList(),
    val completeListState: CompleteListUiState = CompleteListUiState(),
)

data class EditListUiState(
    val isVisible: Boolean = false,
    val listId: String = "",
    val name: String = "",
    val description: String = "",
    val isRecurring: Boolean = false,
    val isSubmitting: Boolean = false,
    @StringRes val errorMessageRes: Int? = null,
) {
    val canSubmit: Boolean = name.isNotBlank() && !isSubmitting
}

data class DeleteListUiState(
    val isVisible: Boolean = false,
    val listId: String = "",
    val listName: String = "",
    val isDeleting: Boolean = false,
)

data class PantryOption(
    val id: String,
    val name: String,
)

data class CompleteListUiState(
    val isVisible: Boolean = false,
    val listId: String = "",
    val listName: String = "",
    val pantryOptions: List<PantryOption> = emptyList(),
    val selectedPantryId: String? = null,
    val isSubmitting: Boolean = false,
    @StringRes val errorMessageRes: Int? = null,
)
