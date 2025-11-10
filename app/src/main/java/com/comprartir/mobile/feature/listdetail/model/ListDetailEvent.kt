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
}
