package com.comprartir.mobile.feature.listdetail.model

import androidx.annotation.StringRes
import com.comprartir.mobile.feature.listdetail.data.ListDetailItem

enum class CategorySelectionTarget {
    AddProduct,
    EditProduct,
}

data class ListDetailUiState(
    val listId: String,
    val title: String = "",
    val subtitle: String = "",
    val items: List<ListDetailItem> = emptyList(),
    val hideCompleted: Boolean = false,
    val filtersExpanded: Boolean = true,
    val selectedCategoryFilterId: String? = null,
    val isLoading: Boolean = true,
    @StringRes val errorMessageRes: Int? = null,
    val addProductState: AddProductUiState = AddProductUiState(),
    val shareState: ShareUiState = ShareUiState(),
    val editListState: EditListDialogState = EditListDialogState(),
    val deleteListState: DeleteListDialogState = DeleteListDialogState(),
    val categories: List<CategoryUi> = emptyList(),
    val editProductState: EditProductDialogState = EditProductDialogState(),
    val createCategoryState: CreateCategoryDialogState = CreateCategoryDialogState(),
) {
    val totalItems: Int get() = items.size
    val completedItems: Int get() = items.count { it.isCompleted }
    val progressFraction: Float
        get() = if (totalItems == 0) 0f else completedItems / totalItems.toFloat()
    val visibleItems: List<ListItemUi>
        get() = items
            .filter { item ->
                (!hideCompleted || !item.isCompleted) &&
                    (selectedCategoryFilterId.isNullOrBlank() || item.categoryId == selectedCategoryFilterId)
            }
            .map { item ->
                val unitLabel = item.unit?.takeIf { it.isNotBlank() }?.let { " $it" } ?: ""
                ListItemUi(
                    id = item.id,
                    name = item.name,
                    quantityLabel = item.quantity + unitLabel,
                    isCompleted = item.isCompleted,
                    notes = item.notes,
                    categoryId = item.categoryId,
                )
            }
}

data class ListItemUi(
    val id: String,
    val name: String,
    val quantityLabel: String,
    val isCompleted: Boolean,
    val notes: String? = null,
    val categoryId: String? = null,
)

data class AddProductUiState(
    val name: String = "",
    val quantity: String = "",
    val unit: String = "",
    val isSubmitting: Boolean = false,
    val categoryId: String? = null,
    val categoryChanged: Boolean = false,
    @StringRes val errorMessageRes: Int? = null,
) {
    val canSubmit: Boolean get() = name.isNotBlank() && !isSubmitting
}

data class EditProductDialogState(
    val isVisible: Boolean = false,
    val itemId: String = "",
    val name: String = "",
    val quantity: String = "",
    val unit: String = "",
    val categoryId: String? = null,
    val categoryChanged: Boolean = false,
    val isSubmitting: Boolean = false,
    @StringRes val errorMessageRes: Int? = null,
) {
    val canSubmit: Boolean = name.isNotBlank() && !isSubmitting
}

data class ShareUiState(
    val email: String = "",
    val link: String = "",
    val isInviting: Boolean = false,
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

data class CategoryUi(
    val id: String?,
    val name: String? = null,
    @StringRes val nameRes: Int? = null,
)

data class CreateCategoryDialogState(
    val isVisible: Boolean = false,
    val name: String = "",
    val isSubmitting: Boolean = false,
    val target: CategorySelectionTarget = CategorySelectionTarget.AddProduct,
    @StringRes val errorMessageRes: Int? = null,
) {
    val canSubmit: Boolean = name.isNotBlank() && !isSubmitting
}
