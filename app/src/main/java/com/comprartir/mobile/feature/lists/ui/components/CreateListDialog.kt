package com.comprartir.mobile.feature.lists.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.comprartir.mobile.feature.lists.model.CreateListUiState

@Composable
fun CreateListDialog(
    state: CreateListUiState,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onRecurringChange: (Boolean) -> Unit,
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
                    text = stringResource(id = R.string.lists_create_dialog_title),
                    style = MaterialTheme.typography.titleLarge,
                )
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = state.name,
                    onValueChange = onNameChange,
                    shape = ComprartirPillShape,
                    singleLine = true,
                    label = { Text(stringResource(id = R.string.lists_create_dialog_name)) },
                )
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    value = state.description,
                    onValueChange = onDescriptionChange,
                    shape = ComprartirPillShape,
                    label = { Text(stringResource(id = R.string.lists_create_dialog_description)) },
                    supportingText = {
                        Text(
                            text = stringResource(id = R.string.lists_create_dialog_optional_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.small),
                ) {
                    Checkbox(checked = state.isRecurring, onCheckedChange = onRecurringChange)
                    Text(
                        text = stringResource(id = R.string.lists_create_dialog_recurring),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                state.errorMessageRes?.let { error ->
                    Text(
                        text = stringResource(id = error),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing.small, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(text = stringResource(id = R.string.dialog_cancel))
                    }
                    Button(
                        onClick = onConfirm,
                        enabled = state.canSubmit,
                        shape = ComprartirPillShape,
                    ) {
                        Text(text = stringResource(id = R.string.lists_create_dialog_confirm))
                    }
                }
            }
        }
    }
}
