package com.comprartir.mobile.lists.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.comprartir.mobile.R
import com.comprartir.mobile.core.designsystem.LocalSpacing
import com.comprartir.mobile.core.navigation.NavigationIntent

@Composable
fun ShareListRoute(
    onNavigate: (NavigationIntent) -> Unit,
    viewModel: ShareListViewModel = hiltViewModel(),
) {
    ShareListScreen(listId = viewModel.listId)
}

@Composable
fun ShareListScreen(listId: String) {
    val spacing = LocalSpacing.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(spacing.large),
    ) {
        Text(text = stringResource(id = R.string.share_list_heading, listId.ifBlank { "N/A" }))
        Button(onClick = { /* TODO: Share via intent */ }) {
            Text(text = stringResource(id = R.string.share_list_button))
        }
    }
}
