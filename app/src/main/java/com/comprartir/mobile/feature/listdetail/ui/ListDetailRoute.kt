package com.comprartir.mobile.feature.listdetail.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.comprartir.mobile.R
import com.comprartir.mobile.core.ui.LocalAppBarTitle
import com.comprartir.mobile.feature.listdetail.model.ListDetailEffect
import com.comprartir.mobile.feature.listdetail.model.ListDetailEvent
import com.comprartir.mobile.feature.listdetail.viewmodel.ListDetailViewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun ListDetailRoute(
    onBack: () -> Unit,
    onOpenShareManagement: (String, String) -> Unit,
    windowSizeClass: WindowSizeClass? = null,
    contentPadding: PaddingValues = PaddingValues(),
    viewModel: ListDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val isTabletLayout = windowSizeClass?.widthSizeClass?.let { it >= WindowWidthSizeClass.Medium } ?: false
    val appBarTitleState = LocalAppBarTitle.current
    val resolvedAppBarTitle = state.name.ifBlank { context.getString(R.string.lists_default_title) }

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
                is ListDetailEffect.ShowError -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                ListDetailEffect.NavigateBack -> {
                    onBack()
                }
            }
        }
    }

    LaunchedEffect(resolvedAppBarTitle) {
        appBarTitleState.value = resolvedAppBarTitle
    }
    DisposableEffect(Unit) {
        onDispose { appBarTitleState.value = null }
    }

    ListDetailScreen(
        state = state,
        onEvent = viewModel::onEvent,
        onBack = onBack,
        onOpenShareManagement = onOpenShareManagement,
        snackbarHostState = snackbarHostState,
        isTabletLayout = isTabletLayout,
        contentPadding = contentPadding,
    )
}
