package com.comprartir.mobile.feature.home.model

data class HomeUiState(
    val userName: String = "",
    val recentLists: List<RecentListUi> = emptyList(),
    val sharedLists: List<SharedListUi> = emptyList(),
    val recentActivity: List<ActivityUi> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)
