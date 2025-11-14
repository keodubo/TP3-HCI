package com.comprartir.mobile.categories.model

import androidx.annotation.StringRes

sealed interface CategoriesEffect {
    data class ShowMessage(@StringRes val messageRes: Int) : CategoriesEffect
    data class ShowError(val message: String) : CategoriesEffect
}
