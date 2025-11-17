package com.comprartir.mobile.core.ui

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.staticCompositionLocalOf

val LocalAppBarTitle = staticCompositionLocalOf<MutableState<String?>> {
    error("LocalAppBarTitle is not provided")
}
