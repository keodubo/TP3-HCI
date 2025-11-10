package com.comprartir.mobile.feature.lists.model

sealed interface ListsEvent {
    data class SearchQueryChanged(val query: String) : ListsEvent
    data class SortOptionSelected(val option: SortOption) : ListsEvent
    data class SortDirectionSelected(val direction: SortDirection) : ListsEvent
    data class ListTypeSelected(val type: ListTypeFilter) : ListsEvent
    data class OpenList(val listId: String) : ListsEvent
    data object CreateList : ListsEvent
    data object DismissCreateList : ListsEvent
    data class CreateListNameChanged(val value: String) : ListsEvent
    data class CreateListDescriptionChanged(val value: String) : ListsEvent
    data class CreateListRecurringChanged(val value: Boolean) : ListsEvent
    data object ConfirmCreateList : ListsEvent
    data object ToggleFilters : ListsEvent
    data object Refresh : ListsEvent
}
