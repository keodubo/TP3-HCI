package com.comprartir.mobile.feature.home.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.comprartir.mobile.core.navigation.AppDestination
import com.comprartir.mobile.core.navigation.NavigationIntent
import com.comprartir.mobile.feature.home.viewmodel.HomeViewModel
import com.comprartir.mobile.feature.home.model.HomeUiState

@Composable
fun HomeRoute(
    onNavigate: (NavigationIntent) -> Unit,
    windowSizeClass: WindowSizeClass? = null,
    contentPadding: PaddingValues = PaddingValues(),
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state: HomeUiState = viewModel.uiState.collectAsStateWithLifecycle().value
    HomeScreen(
        state = state,
        onCreateList = { 
            android.util.Log.d("HomeRoute", "🔥 Create list clicked - navigating with openCreate=true")
            onNavigate(NavigationIntent(AppDestination.Lists, mapOf("openCreate" to "true"))) 
        },
        onViewAllLists = { onNavigate(NavigationIntent(AppDestination.Lists)) },
        onRecentListClick = { listId ->
            onNavigate(
                NavigationIntent(
                    destination = AppDestination.ListDetails,
                    arguments = mapOf("listId" to listId),
                )
            )
        },
        onSharedListClick = { listId ->
            onNavigate(
                NavigationIntent(
                    destination = AppDestination.ListDetails,
                    arguments = mapOf("listId" to listId),
                )
            )
        },
        onRefresh = viewModel::refresh,
        windowSizeClass = windowSizeClass,
        contentPadding = contentPadding,
    )
}
