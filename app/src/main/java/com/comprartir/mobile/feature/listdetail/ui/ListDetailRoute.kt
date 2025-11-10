package com.comprartir.mobile.feature.listdetail.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.comprartir.mobile.R
import com.comprartir.mobile.feature.listdetail.model.ListDetailEffect
import com.comprartir.mobile.feature.listdetail.model.ListDetailEvent
import com.comprartir.mobile.feature.listdetail.viewmodel.ListDetailViewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun ListDetailRoute(
    onBack: () -> Unit,
    windowSizeClass: WindowSizeClass? = null,
    contentPadding: PaddingValues = PaddingValues(),
    viewModel: ListDetailViewModel = hiltViewModel(),
) {
    val state = viewModel.state.collectAsStateWithLifecycle().value
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val isTabletLayout = windowSizeClass?.widthSizeClass?.let { it >= WindowWidthSizeClass.Medium } ?: false

    LaunchedEffect(Unit) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                is ListDetailEffect.ShowUndoDelete -> {
                    val result = snackbarHostState.showSnackbar(
                        message = context.getString(R.string.list_detail_item_deleted, effect.item.name),
                        actionLabel = context.getString(R.string.action_undo),
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.onEvent(ListDetailEvent.UndoDelete)
                    }
                }
                is ListDetailEffect.ShowMessage -> {
                    snackbarHostState.showSnackbar(context.getString(effect.messageRes))
                }
            }
        }
    }

    ListDetailScreen(
        state = state,
        onEvent = viewModel::onEvent,
        onBack = onBack,
        snackbarHostState = snackbarHostState,
        isTabletLayout = isTabletLayout,
        contentPadding = contentPadding,
    )
}
