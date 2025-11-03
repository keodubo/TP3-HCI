package com.comprartir.mobile.shared.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.comprartir.mobile.R
import com.comprartir.mobile.core.designsystem.LocalSpacing

@Composable
fun OptionalFeaturePlaceholder(featureLabel: Int) {
    val spacing = LocalSpacing.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(spacing.large),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = stringResource(id = featureLabel))
    }
}
