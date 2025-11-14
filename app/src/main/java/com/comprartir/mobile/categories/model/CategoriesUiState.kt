package com.comprartir.mobile.categories.model

import androidx.annotation.StringRes

data class CategoriesUiState(
    val categories: List<CategoryItemUi> = emptyList(),
    val isLoading: Boolean = true,
    val dialogState: CategoryDialogState = CategoryDialogState(),
    val deleteState: DeleteCategoryState = DeleteCategoryState(),
)

data class CategoryItemUi(
    val id: String,
    val name: String,
    val description: String? = null,
)

enum class CategoryDialogMode { Create, Edit }

data class CategoryDialogState(
    val isVisible: Boolean = false,
    val mode: CategoryDialogMode = CategoryDialogMode.Create,
    val categoryId: String? = null,
    val name: String = "",
    val isSubmitting: Boolean = false,
    @StringRes val errorMessageRes: Int? = null,
) {
    val canSubmit: Boolean = name.isNotBlank() && !isSubmitting
}

data class DeleteCategoryState(
    val categoryId: String? = null,
    val categoryName: String = "",
    val isVisible: Boolean = false,
    val isSubmitting: Boolean = false,
)
