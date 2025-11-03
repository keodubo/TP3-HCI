package com.comprartir.mobile.lists.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ShareListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    val listId: String = savedStateHandle.get<String>("listId") ?: ""

    // TODO: Provide sharing link generation once backend APIs are ready.
}
