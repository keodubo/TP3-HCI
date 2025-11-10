package com.comprartir.mobile.feature.lists.model

import androidx.annotation.StringRes

data class ListsUiState(
    val searchQuery: String = "",
    val sortOption: SortOption = SortOption.RECENT,
    val sortDirection: SortDirection = SortDirection.DESCENDING,
    val listType: ListTypeFilter = ListTypeFilter.ALL,
    val lists: List<ShoppingListUi> = emptyList(),
    val summary: ListsSummaryUi = ListsSummaryUi(),
    val isLoading: Boolean = true,
    val isFiltersExpanded: Boolean = false,
    val errorMessage: String? = null,
    val createListState: CreateListUiState = CreateListUiState(),
)

data class CreateListUiState(
    val isVisible: Boolean = false,
    val name: String = "",
    val description: String = "",
    val isRecurring: Boolean = false,
    val isSubmitting: Boolean = false,
    @StringRes val errorMessageRes: Int? = null,
) {
    val canSubmit: Boolean = name.isNotBlank() && !isSubmitting
}
