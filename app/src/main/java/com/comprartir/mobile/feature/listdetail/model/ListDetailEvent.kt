package com.comprartir.mobile.feature.listdetail.model

sealed interface ListDetailEvent {
    data class ToggleItem(val itemId: String, val completed: Boolean) : ListDetailEvent
    data class DeleteItem(val itemId: String) : ListDetailEvent
    data object UndoDelete : ListDetailEvent
    data object ToggleHideCompleted : ListDetailEvent
    data object ToggleFilters : ListDetailEvent
    data class AddProductNameChanged(val value: String) : ListDetailEvent
    data class AddProductQuantityChanged(val value: String) : ListDetailEvent
    data class AddProductUnitChanged(val value: String) : ListDetailEvent
    data object SubmitNewProduct : ListDetailEvent
    data class ShareEmailChanged(val value: String) : ListDetailEvent
    data object LinkCopied : ListDetailEvent
    data object Retry : ListDetailEvent
    data object ShowEditDialog : ListDetailEvent
    data class EditListNameChanged(val value: String) : ListDetailEvent
    data class EditListDescriptionChanged(val value: String) : ListDetailEvent
    data object ConfirmEditList : ListDetailEvent
    data object DismissEditDialog : ListDetailEvent
    data object ShowDeleteDialog : ListDetailEvent
    data object ConfirmDeleteList : ListDetailEvent
    data object DismissDeleteDialog : ListDetailEvent
}
