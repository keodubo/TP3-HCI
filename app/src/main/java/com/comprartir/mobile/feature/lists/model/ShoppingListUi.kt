package com.comprartir.mobile.feature.lists.model

data class ShoppingListUi(
    val id: String,
    val name: String,
    val updatedAgo: String,
    val totalItems: Int,
    val acquiredItems: Int,
    val sharedWith: Int,
    val isShared: Boolean,
)
