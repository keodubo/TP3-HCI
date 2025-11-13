package com.comprartir.mobile.feature.listdetail.model

import androidx.annotation.StringRes

sealed interface ListDetailEffect {
    data class ShowUndoDelete(val item: ListItemUi) : ListDetailEffect
    data class ShowMessage(@StringRes val messageRes: Int) : ListDetailEffect
    data object NavigateBack : ListDetailEffect
}

