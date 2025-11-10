package com.comprartir.mobile.feature.listdetail.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.comprartir.mobile.R
import com.comprartir.mobile.feature.listdetail.data.ListDetailItem
import com.comprartir.mobile.feature.listdetail.data.ListDetailRepository
import com.comprartir.mobile.feature.listdetail.model.AddProductUiState
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
            ListDetailEvent.SubmitNewProduct -> addNewProduct()
            is ListDetailEvent.ShareEmailChanged -> _state.update { it.copy(shareState = it.shareState.copy(email = event.value)) }
            ListDetailEvent.LinkCopied -> emitMessage(R.string.list_detail_link_copied)
            ListDetailEvent.Retry -> observeList(force = true)
        }
    }

    private fun observeList(force: Boolean = false) {
        if (force) {
            _state.update { it.copy(isLoading = true, errorMessageRes = null) }
        }
        viewModelScope.launch {
            repository.observeList(listId).collectLatest { detail ->
                _state.update {
                    it.copy(
                        title = detail.title,
                        subtitle = detail.subtitle,
                        items = detail.items.map { item -> item.toUi() },
                        shareState = it.shareState.copy(link = detail.shareLink),
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
                        lastDeletedUi = removed.toUi()
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
                )
            }.onSuccess {
                updateAddProductState { AddProductUiState() }
            }.onFailure {
                updateAddProductState { it.copy(isSubmitting = false, errorMessageRes = R.string.list_detail_error_add) }
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

    private fun ListDetailItem.toUi(): ListItemUi {
        val unitLabel = unit?.takeIf { it.isNotBlank() }?.let { " $it" } ?: ""
        return ListItemUi(
            id = id,
            name = name,
            quantityLabel = quantity + unitLabel,
            isCompleted = isCompleted,
            notes = notes,
        )
    }
}
