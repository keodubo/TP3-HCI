package com.comprartir.mobile.feature.lists.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.platform.LocalContext
import com.comprartir.mobile.core.navigation.AppDestination
import com.comprartir.mobile.core.navigation.NavigationIntent
import com.comprartir.mobile.feature.lists.model.ListsEffect
import com.comprartir.mobile.feature.lists.viewmodel.ListsViewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun ListsRoute(
    onNavigate: (NavigationIntent) -> Unit,
    contentPadding: PaddingValues = PaddingValues(),
    viewModel: ListsViewModel = hiltViewModel(),
) {
    val state = viewModel.state.collectAsStateWithLifecycle().value
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                is ListsEffect.NavigateToListDetail -> {
                    onNavigate(
                        NavigationIntent(
                            destination = AppDestination.ListDetails,
                            arguments = mapOf("listId" to effect.listId),
                        )
                    )
                }
                is ListsEffect.ShowSnackbar -> {
                    val message = effect.messageArg?.let {
                        context.getString(effect.messageRes, it)
                    } ?: context.getString(effect.messageRes)
                    snackbarHostState.showSnackbar(message)
                }
            }
        }
    }
    ListsScreen(
        state = state,
        onEvent = viewModel::onEvent,
        snackbarHostState = snackbarHostState,
        contentPadding = contentPadding,
    )
}
