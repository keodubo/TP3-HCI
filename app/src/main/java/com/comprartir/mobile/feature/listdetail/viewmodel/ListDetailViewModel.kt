package com.comprartir.mobile.feature.listdetail.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.comprartir.mobile.R
import com.comprartir.mobile.feature.listdetail.data.AddItemResult
import com.comprartir.mobile.feature.listdetail.data.ListDetailItem
import com.comprartir.mobile.feature.listdetail.data.ListDetailRepository
import com.comprartir.mobile.feature.listdetail.model.AddProductUiState
import com.comprartir.mobile.feature.listdetail.model.CategorySelectionTarget
import com.comprartir.mobile.feature.listdetail.model.CategoryUi
import com.comprartir.mobile.feature.listdetail.model.DeleteListDialogState
import com.comprartir.mobile.feature.listdetail.model.EditListDialogState
import com.comprartir.mobile.feature.listdetail.model.EditProductDialogState
import com.comprartir.mobile.feature.listdetail.model.CreateCategoryDialogState
import com.comprartir.mobile.feature.listdetail.model.ListDetailEffect
import com.comprartir.mobile.feature.listdetail.model.ListDetailEvent
import com.comprartir.mobile.feature.listdetail.model.ListDetailUiState
import com.comprartir.mobile.feature.listdetail.model.ListItemUi
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ListDetailViewModel @Inject constructor(
    private val repository: ListDetailRepository,
    private val shoppingListsRepository: com.comprartir.mobile.lists.data.ShoppingListsRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val listId: String = savedStateHandle["listId"] ?: ""

    private val _state = MutableStateFlow(ListDetailUiState(listId = listId))
    val state: StateFlow<ListDetailUiState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<ListDetailEffect>()
    val effects: SharedFlow<ListDetailEffect> = _effects.asSharedFlow()

    private var lastDeletedDomain: ListDetailItem? = null
    private var lastDeletedUi: ListItemUi? = null

    init {
        if (listId.isBlank()) {
            _state.update { it.copy(isLoading = false, errorMessageRes = R.string.list_detail_error_not_found) }
        } else {
            observeList()
        }
        observeCategories()
    }

    fun onEvent(event: ListDetailEvent) {
        when (event) {
            is ListDetailEvent.ToggleItem -> toggleItem(event.itemId, event.completed)
            is ListDetailEvent.DeleteItem -> deleteItem(event.itemId)
            ListDetailEvent.UndoDelete -> undoDelete()
            ListDetailEvent.ToggleHideCompleted -> _state.update { it.copy(hideCompleted = !it.hideCompleted) }
            ListDetailEvent.ToggleFilters -> _state.update { it.copy(filtersExpanded = !it.filtersExpanded) }
            is ListDetailEvent.AddProductNameChanged -> updateAddProductState { it.copy(name = event.value, errorMessageRes = null) }
            is ListDetailEvent.AddProductQuantityChanged -> updateAddProductState { it.copy(quantity = event.value) }
            is ListDetailEvent.AddProductUnitChanged -> updateAddProductState { it.copy(unit = event.value) }
            is ListDetailEvent.AddProductCategoryChanged -> updateAddProductState { it.copy(categoryId = event.value, categoryChanged = true) }
            ListDetailEvent.SubmitNewProduct -> addNewProduct()
            is ListDetailEvent.ShareEmailChanged -> _state.update { it.copy(shareState = it.shareState.copy(email = event.value)) }
            ListDetailEvent.LinkCopied -> emitMessage(R.string.list_detail_link_copied)
            ListDetailEvent.Retry -> observeList(force = true)
            ListDetailEvent.ShowEditDialog -> showEditDialog()
            is ListDetailEvent.EditListNameChanged -> _state.update { it.copy(editListState = it.editListState.copy(name = event.value, errorMessageRes = null)) }
            is ListDetailEvent.EditListDescriptionChanged -> _state.update { it.copy(editListState = it.editListState.copy(description = event.value)) }
            ListDetailEvent.ConfirmEditList -> confirmEditList()
            ListDetailEvent.DismissEditDialog -> _state.update { it.copy(editListState = EditListDialogState()) }
            ListDetailEvent.ShowDeleteDialog -> showDeleteDialog()
            ListDetailEvent.ConfirmDeleteList -> confirmDeleteList()
            ListDetailEvent.DismissDeleteDialog -> _state.update { it.copy(deleteListState = DeleteListDialogState()) }
            is ListDetailEvent.FilterCategoryChanged -> _state.update { it.copy(selectedCategoryFilterId = event.categoryId) }
            is ListDetailEvent.ShowEditProductDialog -> showEditProductDialog(event.itemId)
            ListDetailEvent.DismissEditProductDialog -> _state.update { it.copy(editProductState = EditProductDialogState()) }
            is ListDetailEvent.EditProductNameChanged -> updateEditProductState { it.copy(name = event.value, errorMessageRes = null) }
            is ListDetailEvent.EditProductQuantityChanged -> updateEditProductState { it.copy(quantity = event.value) }
            is ListDetailEvent.EditProductUnitChanged -> updateEditProductState { it.copy(unit = event.value) }
            is ListDetailEvent.EditProductCategoryChanged -> updateEditProductState { it.copy(categoryId = event.categoryId, categoryChanged = true) }
            ListDetailEvent.ConfirmEditProduct -> confirmEditProduct()
            is ListDetailEvent.ShowCreateCategoryDialog -> showCreateCategoryDialog(event.target)
            ListDetailEvent.DismissCreateCategoryDialog -> dismissCreateCategoryDialog()
            is ListDetailEvent.CreateCategoryNameChanged -> updateCreateCategoryState { it.copy(name = event.value, errorMessageRes = null) }
            ListDetailEvent.ConfirmCreateCategory -> confirmCreateCategory()
        }
    }

    private fun observeList(force: Boolean = false) {
        if (force) {
            _state.update { it.copy(isLoading = true, errorMessageRes = null) }
        }
        viewModelScope.launch {
            repository.observeList(listId).collectLatest { detail ->
                _state.update { current ->
                    current.copy(
                        title = detail.title,
                        subtitle = detail.subtitle,
                        items = detail.items,
                        shareState = current.shareState.copy(link = detail.shareLink),
                        isLoading = false,
                        errorMessageRes = null,
                    )
                }
            }
        }
    }

    private fun toggleItem(itemId: String, completed: Boolean) {
        viewModelScope.launch {
            runCatching { repository.toggleItem(listId, itemId, completed) }
                .onFailure {
                    _state.update { it.copy(errorMessageRes = R.string.list_detail_error_toggle) }
                }
        }
    }

    private fun deleteItem(itemId: String) {
        viewModelScope.launch {
            runCatching { repository.deleteItem(listId, itemId) }
                .onSuccess { removed ->
                    if (removed != null) {
                        lastDeletedDomain = removed
                        lastDeletedUi = removed.toUiModel()
                        lastDeletedUi?.let {
                            _effects.emit(ListDetailEffect.ShowUndoDelete(it))
                        }
                    }
                }.onFailure {
                    _state.update { it.copy(errorMessageRes = R.string.list_detail_error_delete) }
                }
        }
    }

    private fun undoDelete() {
        val item = lastDeletedDomain ?: return
        viewModelScope.launch {
            runCatching { repository.restoreItem(listId, item) }
                .onSuccess {
                    lastDeletedDomain = null
                    lastDeletedUi = null
                }.onFailure {
                    _state.update { it.copy(errorMessageRes = R.string.list_detail_error_restore) }
                }
        }
    }

    private fun addNewProduct() {
        val current = state.value.addProductState
        if (current.name.isBlank() || current.isSubmitting) {
            if (current.name.isBlank()) {
                updateAddProductState { it.copy(errorMessageRes = R.string.list_detail_add_name_error) }
            }
            return
        }
        updateAddProductState { it.copy(isSubmitting = true, errorMessageRes = null) }
        viewModelScope.launch {
            runCatching {
                repository.addItem(
                    listId = listId,
                    name = current.name.trim(),
                    quantity = current.quantity.ifBlank { "1" },
                    unit = current.unit.trim().ifBlank { null },
                    categoryId = current.categoryId,
                    overrideCategory = current.categoryChanged,
                )
            }.onSuccess { result ->
                when (result) {
                    is AddItemResult.Added -> {
                        updateAddProductState { AddProductUiState() }
                        emitMessage(R.string.list_detail_product_added_success)
                    }
                    is AddItemResult.AlreadyExists -> {
                        updateAddProductState { AddProductUiState() }
                        emitMessage(R.string.list_detail_product_already_exists)
                    }
                }
            }.onFailure { error ->
                android.util.Log.e("ListDetailViewModel", "Error adding product: ${error.message}", error)
                // Show specific error message if available
                val errorMessage = error.message ?: "No pudimos agregar el producto."
                _state.update { it.copy(errorMessageRes = R.string.list_detail_error_add) }
                updateAddProductState { it.copy(isSubmitting = false, errorMessageRes = null) }
                // Emit error as a toast/snackbar effect
                viewModelScope.launch {
                    _effects.emit(ListDetailEffect.ShowError(errorMessage))
                }
            }
        }
    }

    private fun updateAddProductState(transform: (AddProductUiState) -> AddProductUiState) {
        _state.update { it.copy(addProductState = transform(it.addProductState)) }
    }

    private fun emitMessage(messageRes: Int) {
        viewModelScope.launch {
            _effects.emit(ListDetailEffect.ShowMessage(messageRes))
        }
    }

    private fun observeCategories() {
        viewModelScope.launch {
            repository.observeCategories().collectLatest { categories ->
                val options = listOf(CategoryUi(id = null, nameRes = R.string.list_detail_category_none)) +
                    categories
                    .sortedBy { it.name.lowercase() }
                    .map { CategoryUi(id = it.id, name = it.name) }
                _state.update { current ->
                    val sanitizedAddCategory = sanitizeCategorySelection(current.addProductState.categoryId, options)
                    val sanitizedEditCategory = sanitizeCategorySelection(current.editProductState.categoryId, options)
                    val sanitizedFilter = sanitizeCategorySelection(current.selectedCategoryFilterId, options)
                    current.copy(
                        categories = options,
                        addProductState = current.addProductState.copy(categoryId = sanitizedAddCategory),
                        editProductState = current.editProductState.copy(categoryId = sanitizedEditCategory),
                        selectedCategoryFilterId = sanitizedFilter,
                    )
                }
            }
        }
    }

    private fun sanitizeCategorySelection(selection: String?, options: List<CategoryUi>): String? =
        selection?.takeIf { id -> options.any { it.id == id } }

    private fun showEditDialog() {
        val currentTitle = _state.value.title
        _state.update {
            it.copy(
                editListState = EditListDialogState(
                    isVisible = true,
                    name = currentTitle,
                    description = "",
                )
            )
        }
    }

    private fun confirmEditList() {
        val currentState = _state.value.editListState
        if (currentState.isSubmitting || currentState.name.isBlank()) {
            if (currentState.name.isBlank()) {
                _state.update { it.copy(editListState = it.editListState.copy(errorMessageRes = R.string.lists_create_dialog_name_error)) }
            }
            return
        }

        _state.update { it.copy(editListState = it.editListState.copy(isSubmitting = true, errorMessageRes = null)) }

        viewModelScope.launch {
            runCatching {
                shoppingListsRepository.updateList(
                    listId = listId,
                    name = currentState.name.trim(),
                    description = currentState.description.trim().ifBlank { null }
                )
            }.onSuccess {
                _state.update { it.copy(editListState = EditListDialogState()) }
            }.onFailure {
                _state.update { it.copy(editListState = it.editListState.copy(isSubmitting = false, errorMessageRes = R.string.error_updating_list)) }
            }
        }
    }

    private fun showDeleteDialog() {
        _state.update {
            it.copy(deleteListState = DeleteListDialogState(isVisible = true))
        }
    }

    private fun confirmDeleteList() {
        val currentState = _state.value.deleteListState
        if (currentState.isDeleting) return

        _state.update { it.copy(deleteListState = it.deleteListState.copy(isDeleting = true)) }

        viewModelScope.launch {
            runCatching {
                shoppingListsRepository.deleteList(listId)
            }.onSuccess {
                _state.update { it.copy(deleteListState = DeleteListDialogState()) }
                _effects.emit(ListDetailEffect.NavigateBack)
            }.onFailure {
                _state.update { it.copy(deleteListState = it.deleteListState.copy(isDeleting = false)) }
                emitMessage(R.string.error_deleting_list)
            }
        }
    }

    private fun showEditProductDialog(itemId: String) {
        val item = _state.value.items.find { it.id == itemId } ?: return
        _state.update {
            it.copy(
                editProductState = EditProductDialogState(
                    isVisible = true,
                    itemId = item.id,
                    name = item.name,
                    quantity = item.quantity,
                    unit = item.unit.orEmpty(),
                    categoryId = item.categoryId,
                    categoryChanged = false,
                )
            )
        }
    }

    private fun updateEditProductState(transform: (EditProductDialogState) -> EditProductDialogState) {
        _state.update { it.copy(editProductState = transform(it.editProductState)) }
    }

    private fun confirmEditProduct() {
        val dialogState = _state.value.editProductState
        if (!dialogState.isVisible || dialogState.isSubmitting) return
        if (dialogState.itemId.isBlank() || dialogState.name.isBlank()) {
            updateEditProductState { it.copy(errorMessageRes = R.string.list_detail_add_name_error) }
            return
        }

        updateEditProductState { it.copy(isSubmitting = true, errorMessageRes = null) }

        viewModelScope.launch {
            runCatching {
                repository.updateItem(
                    listId = listId,
                    itemId = dialogState.itemId,
                    name = dialogState.name.trim(),
                    quantity = dialogState.quantity.ifBlank { "1" },
                    unit = dialogState.unit.trim().ifBlank { null },
                    categoryId = dialogState.categoryId,
                    overrideCategory = dialogState.categoryChanged,
                )
            }.onSuccess {
                _state.update { it.copy(editProductState = EditProductDialogState()) }
                emitMessage(R.string.list_detail_product_updated_success)
            }.onFailure { error ->
                android.util.Log.e("ListDetailViewModel", "Error updating product: ${error.message}", error)
                updateEditProductState { it.copy(isSubmitting = false) }
                val errorMessage = error.message ?: "No pudimos actualizar el producto."
                viewModelScope.launch { _effects.emit(ListDetailEffect.ShowError(errorMessage)) }
            }
        }
    }

    private fun showCreateCategoryDialog(target: CategorySelectionTarget) {
        _state.update {
            it.copy(createCategoryState = CreateCategoryDialogState(isVisible = true, target = target))
        }
    }

    private fun dismissCreateCategoryDialog() {
        _state.update { it.copy(createCategoryState = CreateCategoryDialogState()) }
    }

    private fun updateCreateCategoryState(transform: (CreateCategoryDialogState) -> CreateCategoryDialogState) {
        _state.update { it.copy(createCategoryState = transform(it.createCategoryState)) }
    }

    private fun confirmCreateCategory() {
        val dialogState = _state.value.createCategoryState
        if (dialogState.name.isBlank()) {
            updateCreateCategoryState { it.copy(errorMessageRes = R.string.list_detail_category_name_error) }
            return
        }
        if (dialogState.isSubmitting) return

        updateCreateCategoryState { it.copy(isSubmitting = true, errorMessageRes = null) }

        viewModelScope.launch {
            runCatching { repository.createCategory(dialogState.name.trim()) }
                .onSuccess { category ->
                    when (dialogState.target) {
                        CategorySelectionTarget.AddProduct -> updateAddProductState {
                            it.copy(categoryId = category.id, categoryChanged = true)
                        }
                        CategorySelectionTarget.EditProduct -> updateEditProductState {
                            it.copy(categoryId = category.id, categoryChanged = true)
                        }
                    }
                    _state.update { it.copy(createCategoryState = CreateCategoryDialogState()) }
                    emitMessage(R.string.list_detail_category_created_success)
                }
                .onFailure { error ->
                    updateCreateCategoryState { it.copy(isSubmitting = false) }
                    val message = error.message ?: "No pudimos crear la categoría."
                    viewModelScope.launch {
                        _effects.emit(ListDetailEffect.ShowError(message))
                    }
                }
        }
    }

    private fun ListDetailItem.toUiModel(): ListItemUi {
        val unitLabel = unit?.takeIf { it.isNotBlank() }?.let { " $it" } ?: ""
        return ListItemUi(
            id = id,
            name = name,
            quantityLabel = quantity + unitLabel,
            isCompleted = isCompleted,
            notes = notes,
            categoryId = categoryId,
        )
    }
}
