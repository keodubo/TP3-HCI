package com.comprartir.mobile.feature.lists.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.comprartir.mobile.feature.lists.data.ListsRepository
import com.comprartir.mobile.feature.lists.model.ListTypeFilter
import com.comprartir.mobile.feature.lists.model.ListsEvent
import com.comprartir.mobile.feature.lists.model.ListsSummaryUi
import com.comprartir.mobile.feature.lists.model.ListsUiState
import com.comprartir.mobile.feature.lists.model.SortDirection
import com.comprartir.mobile.feature.lists.model.SortOption
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.comprartir.mobile.R
import com.comprartir.mobile.feature.lists.model.CreateListUiState
import com.comprartir.mobile.feature.lists.model.ListsEffect

@HiltViewModel
class ListsViewModel @Inject constructor(
    private val repository: ListsRepository, // TODO: swap fake repository for real data source when backend integration lands.
) : ViewModel() {

    private val _state = MutableStateFlow(ListsUiState())
    val state: StateFlow<ListsUiState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<ListsEffect>()
    val effects: SharedFlow<ListsEffect> = _effects.asSharedFlow()

    private var searchJob: Job? = null

    init {
        observeLists()
        observeSummary()
    }

    fun onEvent(event: ListsEvent) {
        when (event) {
            is ListsEvent.SearchQueryChanged -> updateSearch(event.query)
            is ListsEvent.SortOptionSelected -> {
                _state.update { it.copy(sortOption = event.option) }
                triggerSearch()
            }
            is ListsEvent.SortDirectionSelected -> {
                _state.update { it.copy(sortDirection = event.direction) }
                triggerSearch()
            }
            is ListsEvent.ListTypeSelected -> {
                _state.update { it.copy(listType = event.type) }
                triggerSearch()
            }
            ListsEvent.ToggleFilters -> _state.update { it.copy(isFiltersExpanded = !it.isFiltersExpanded) }
            ListsEvent.Refresh -> observeLists(force = true)
            is ListsEvent.OpenList -> {
                viewModelScope.launch {
                    _effects.emit(ListsEffect.NavigateToListDetail(event.listId))
                }
            }
            ListsEvent.CreateList -> showCreateListDialog(true)
            ListsEvent.DismissCreateList -> showCreateListDialog(false)
            is ListsEvent.CreateListNameChanged -> updateCreateListState { it.copy(name = event.value, errorMessageRes = null) }
            is ListsEvent.CreateListDescriptionChanged -> updateCreateListState { it.copy(description = event.value) }
            is ListsEvent.CreateListRecurringChanged -> updateCreateListState { it.copy(isRecurring = event.value) }
            ListsEvent.ConfirmCreateList -> submitCreateList()
        }
    }

    private fun observeLists(force: Boolean = false) {
        viewModelScope.launch {
            if (force) {
                _state.update { it.copy(isLoading = true) }
            }
            repository.getLists()
                .collectLatest { lists ->
                    _state.update {
                        it.copy(
                            lists = lists,
                            isLoading = false,
                            errorMessage = null,
                        )
                    }
                }
        }
    }

    private fun observeSummary() {
        viewModelScope.launch {
            repository.getSummary().collectLatest { summary ->
                _state.update { it.copy(summary = summary) }
            }
        }
    }

    private fun updateSearch(query: String) {
        _state.update { it.copy(searchQuery = query) }
        triggerSearch()
    }

    private fun triggerSearch() {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300)
            val current = state.value
            runCatching {
                repository.searchLists(
                    query = current.searchQuery,
                    sortOption = current.sortOption,
                    direction = current.sortDirection,
                    typeFilter = current.listType,
                )
            }.onSuccess { result ->
                _state.update { it.copy(lists = result, errorMessage = null) }
            }.onFailure { throwable ->
                _state.update { it.copy(errorMessage = throwable.message) }
            }
        }
    }

    private fun showCreateListDialog(visible: Boolean) {
        _state.update {
            it.copy(
                createListState = if (visible) {
                    it.createListState.copy(isVisible = true, errorMessageRes = null)
                } else {
                    CreateListUiState()
                },
            )
        }
    }

    private fun updateCreateListState(transform: (CreateListUiState) -> CreateListUiState) {
        _state.update { current ->
            current.copy(createListState = transform(current.createListState))
        }
    }

    private fun submitCreateList() {
        val currentState = state.value.createListState
        if (currentState.isSubmitting) return
        if (currentState.name.isBlank()) {
            updateCreateListState { it.copy(errorMessageRes = R.string.lists_create_dialog_name_error) }
            return
        }
        updateCreateListState { it.copy(isSubmitting = true, errorMessageRes = null) }
        viewModelScope.launch {
            runCatching {
                repository.createList(
                    name = currentState.name.trim(),
                    description = currentState.description.trim(),
                    isRecurring = currentState.isRecurring,
                )
            }.onSuccess { listId ->
                showCreateListDialog(false)
                _effects.emit(ListsEffect.NavigateToListDetail(listId))
            }.onFailure { throwable ->
                updateCreateListState {
                    it.copy(
                        isSubmitting = false,
                        errorMessageRes = R.string.error_creating_list,
                    )
                }
                _effects.emit(ListsEffect.ShowSnackbar(R.string.error_creating_list))
            }
        }
    }
}
