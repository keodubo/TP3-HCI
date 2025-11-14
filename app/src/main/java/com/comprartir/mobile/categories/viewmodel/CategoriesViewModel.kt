package com.comprartir.mobile.categories.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.comprartir.mobile.R
import com.comprartir.mobile.categories.model.CategoriesEffect
import com.comprartir.mobile.categories.model.CategoriesEvent
import com.comprartir.mobile.categories.model.CategoriesUiState
import com.comprartir.mobile.categories.model.CategoryDialogMode
import com.comprartir.mobile.categories.model.CategoryDialogState
import com.comprartir.mobile.categories.model.CategoryItemUi
import com.comprartir.mobile.categories.model.DeleteCategoryState
import com.comprartir.mobile.products.data.ProductsRepository
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
class CategoriesViewModel @Inject constructor(
    private val productsRepository: ProductsRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(CategoriesUiState())
    val state: StateFlow<CategoriesUiState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<CategoriesEffect>()
    val effects: SharedFlow<CategoriesEffect> = _effects.asSharedFlow()

    init {
        observeCategories()
    }

    private fun observeCategories() {
        viewModelScope.launch {
            productsRepository.observeCategories().collectLatest { categories ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        categories = categories.map { category ->
                            CategoryItemUi(id = category.id, name = category.name, description = category.description)
                        },
                    )
                }
            }
        }
    }

    fun onEvent(event: CategoriesEvent) {
        when (event) {
            CategoriesEvent.ShowCreateDialog -> showCreateDialog()
            is CategoriesEvent.ShowEditDialog -> showEditDialog(event.categoryId)
            CategoriesEvent.DismissDialog -> hideDialog()
            is CategoriesEvent.DialogNameChanged -> updateDialogState { it.copy(name = event.value, errorMessageRes = null) }
            CategoriesEvent.ConfirmDialog -> confirmDialog()
            is CategoriesEvent.RequestDelete -> showDeleteDialog(event.categoryId)
            CategoriesEvent.DismissDelete -> hideDeleteDialog()
            CategoriesEvent.ConfirmDelete -> confirmDelete()
        }
    }

    private fun showCreateDialog() {
        _state.update {
            it.copy(dialogState = CategoryDialogState(isVisible = true, mode = CategoryDialogMode.Create))
        }
    }

    private fun showEditDialog(categoryId: String) {
        val category = _state.value.categories.find { it.id == categoryId } ?: return
        _state.update {
            it.copy(
                dialogState = CategoryDialogState(
                    isVisible = true,
                    mode = CategoryDialogMode.Edit,
                    categoryId = category.id,
                    name = category.name,
                )
            )
        }
    }

    private fun hideDialog() {
        _state.update { it.copy(dialogState = CategoryDialogState()) }
    }

    private fun updateDialogState(transform: (CategoryDialogState) -> CategoryDialogState) {
        _state.update { it.copy(dialogState = transform(it.dialogState)) }
    }

    private fun confirmDialog() {
        val dialogState = _state.value.dialogState
        if (dialogState.name.isBlank()) {
            updateDialogState { it.copy(errorMessageRes = R.string.categories_name_error) }
            return
        }
        if (dialogState.isSubmitting) return

        updateDialogState { it.copy(isSubmitting = true, errorMessageRes = null) }

        viewModelScope.launch {
            runCatching {
                when (dialogState.mode) {
                    CategoryDialogMode.Create -> productsRepository.createCategory(dialogState.name.trim())
                    CategoryDialogMode.Edit -> {
                        val id = dialogState.categoryId ?: error("Missing category id")
                        productsRepository.updateCategory(id, dialogState.name.trim(), description = null)
                        null
                    }
                }
            }.onSuccess {
                hideDialog()
                val message = when (dialogState.mode) {
                    CategoryDialogMode.Create -> R.string.categories_created
                    CategoryDialogMode.Edit -> R.string.categories_updated
                }
                _effects.emit(CategoriesEffect.ShowMessage(message))
            }.onFailure { throwable ->
                updateDialogState { it.copy(isSubmitting = false) }
                _effects.emit(CategoriesEffect.ShowError(throwable.message ?: "Error"))
            }
        }
    }

    private fun showDeleteDialog(categoryId: String) {
        val category = _state.value.categories.find { it.id == categoryId } ?: return
        _state.update {
            it.copy(
                deleteState = DeleteCategoryState(
                    categoryId = category.id,
                    categoryName = category.name,
                    isVisible = true,
                )
            )
        }
    }

    private fun hideDeleteDialog() {
        _state.update { it.copy(deleteState = DeleteCategoryState()) }
    }

    private fun confirmDelete() {
        val deleteState = _state.value.deleteState
        val categoryId = deleteState.categoryId ?: return
        if (deleteState.isSubmitting) return

        _state.update { it.copy(deleteState = deleteState.copy(isSubmitting = true)) }

        viewModelScope.launch {
            runCatching {
                productsRepository.deleteCategory(categoryId)
            }.onSuccess {
                _state.update { it.copy(deleteState = DeleteCategoryState()) }
                _effects.emit(CategoriesEffect.ShowMessage(R.string.categories_deleted))
            }.onFailure { throwable ->
                _state.update { it.copy(deleteState = deleteState.copy(isSubmitting = false)) }
                _effects.emit(CategoriesEffect.ShowError(throwable.message ?: "Error"))
            }
        }
    }
}
