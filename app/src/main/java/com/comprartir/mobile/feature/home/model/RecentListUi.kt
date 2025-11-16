package com.comprartir.mobile.feature.home.model

data class RecentListUi(
    val id: String,
    val name: String,
    val date: String,
    val itemCount: Int,
    val completedItemCount: Int,
    val status: String,
    val statusType: ListStatusType,
    val isShared: Boolean,
)

enum class ListStatusType {
    EMPTY,
    PENDING,
    IN_PROGRESS,
    COMPLETE,
}
