package com.comprartir.mobile.feature.lists.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.comprartir.mobile.R
import com.comprartir.mobile.core.designsystem.ComprartirPillShape
import com.comprartir.mobile.core.designsystem.LocalSpacing
import com.comprartir.mobile.lists.presentation.DeleteListUiState

@Composable
fun DeleteListDialog(
    state: DeleteListUiState,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    if (!state.isVisible) return

    val spacing = LocalSpacing.current
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.large),
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(spacing.large),
                verticalArrangement = Arrangement.spacedBy(spacing.medium),
            ) {
                Text(
                    text = stringResource(id = R.string.lists_delete_dialog_title),
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    text = stringResource(id = R.string.lists_delete_dialog_message, state.listName),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing.small, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(
                        onClick = onDismiss,
                        enabled = !state.isDeleting,
                    ) {
                        Text(text = stringResource(id = R.string.dialog_cancel))
                    }
                    Button(
                        onClick = onConfirm,
                        enabled = !state.isDeleting,
                        shape = ComprartirPillShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError,
                        )
                    ) {
                        Text(text = stringResource(id = R.string.lists_delete_dialog_confirm))
                    }
                }
            }
        }
    }
}
