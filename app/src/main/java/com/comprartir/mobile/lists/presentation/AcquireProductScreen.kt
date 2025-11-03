package com.comprartir.mobile.lists.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.comprartir.mobile.core.designsystem.LocalSpacing
import com.comprartir.mobile.core.navigation.NavigationIntent
import com.comprartir.mobile.R

@Composable
fun AcquireProductRoute(
    onNavigate: (NavigationIntent) -> Unit,
) {
    AcquireProductScreen()
}

@Composable
fun AcquireProductScreen() {
    val spacing = LocalSpacing.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(spacing.large),
    ) {
        Text(text = stringResource(id = R.string.acquire_products_heading))
        // TODO: Provide checklist UI that mirrors the Comprartir web application.
    }
}
