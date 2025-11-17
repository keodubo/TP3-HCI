package com.comprartir.mobile.feature.home.model

import java.time.Instant

data class RecentListUi(
    val id: String,
    val name: String,
    val updatedAt: Instant,
    val itemCount: Int,
    val completedItemCount: Int,
    val statusType: ListStatusType,
    val isShared: Boolean,
)

enum class ListStatusType {
    EMPTY,
    PENDING,
    IN_PROGRESS,
    COMPLETE,
}
