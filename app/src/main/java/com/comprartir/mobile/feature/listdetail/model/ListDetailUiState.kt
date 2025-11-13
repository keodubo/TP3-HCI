package com.comprartir.mobile.feature.listdetail.model

import androidx.annotation.StringRes

data class ListDetailUiState(
    val listId: String,
    val title: String = "",
    val subtitle: String = "",
    val items: List<ListItemUi> = emptyList(),
    val hideCompleted: Boolean = false,
    val filtersExpanded: Boolean = true,
    val isLoading: Boolean = true,
    @StringRes val errorMessageRes: Int? = null,
    val addProductState: AddProductUiState = AddProductUiState(),
    val shareState: ShareUiState = ShareUiState(),
    val editListState: EditListDialogState = EditListDialogState(),
    val deleteListState: DeleteListDialogState = DeleteListDialogState(),
) {
    val totalItems: Int get() = items.size
    val completedItems: Int get() = items.count { it.isCompleted }
    val progressFraction: Float
        get() = if (totalItems == 0) 0f else completedItems / totalItems.toFloat()
    val visibleItems: List<ListItemUi>
        get() = if (hideCompleted) items.filterNot { it.isCompleted } else items
}

data class ListItemUi(
    val id: String,
    val name: String,
    val quantityLabel: String,
    val isCompleted: Boolean,
    val notes: String? = null,
)

data class AddProductUiState(
    val name: String = "",
    val quantity: String = "",
    val unit: String = "",
    val isSubmitting: Boolean = false,
    @StringRes val errorMessageRes: Int? = null,
) {
    val canSubmit: Boolean get() = name.isNotBlank() && !isSubmitting
}

data class ShareUiState(
    val email: String = "",
    val link: String = "",
)

data class EditListDialogState(
    val isVisible: Boolean = false,
    val name: String = "",
    val description: String = "",
    val isSubmitting: Boolean = false,
    @StringRes val errorMessageRes: Int? = null,
) {
    val canSubmit: Boolean = name.isNotBlank() && !isSubmitting
}

data class DeleteListDialogState(
    val isVisible: Boolean = false,
    val isDeleting: Boolean = false,
)
