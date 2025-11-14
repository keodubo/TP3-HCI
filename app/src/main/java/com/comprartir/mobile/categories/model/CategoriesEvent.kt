package com.comprartir.mobile.categories.model

sealed interface CategoriesEvent {
    data object ShowCreateDialog : CategoriesEvent
    data class ShowEditDialog(val categoryId: String) : CategoriesEvent
    data object DismissDialog : CategoriesEvent
    data class DialogNameChanged(val value: String) : CategoriesEvent
    data object ConfirmDialog : CategoriesEvent
    data class RequestDelete(val categoryId: String) : CategoriesEvent
    data object DismissDelete : CategoriesEvent
    data object ConfirmDelete : CategoriesEvent
}
