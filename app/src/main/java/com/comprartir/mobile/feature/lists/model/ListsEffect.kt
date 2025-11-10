package com.comprartir.mobile.feature.lists.model

import androidx.annotation.StringRes

sealed interface ListsEffect {
    data class NavigateToListDetail(val listId: String) : ListsEffect
    data class ShowSnackbar(
        @StringRes val messageRes: Int,
        val messageArg: String? = null,
    ) : ListsEffect
}

