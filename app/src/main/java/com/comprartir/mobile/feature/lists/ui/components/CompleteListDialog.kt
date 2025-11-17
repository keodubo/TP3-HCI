package com.comprartir.mobile.feature.lists.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.comprartir.mobile.R
import com.comprartir.mobile.core.designsystem.LocalSpacing
import com.comprartir.mobile.lists.presentation.CompleteListUiState

@Composable
fun CompleteListDialog(
    state: CompleteListUiState,
    onPantrySelected: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    onCompleteWithoutPantry: () -> Unit,
) {
    if (!state.isVisible) return

    AlertDialog(
        onDismissRequest = { if (!state.isSubmitting) onDismiss() },
        containerColor = Color.White,
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !state.isSubmitting && state.pantryOptions.isNotEmpty(),
            ) {
                Text(text = stringResource(id = R.string.lists_complete_yes_pantry))
            }
        },
        dismissButton = {
            TextButton(onClick = { if (!state.isSubmitting) onCompleteWithoutPantry() }) {
                Text(text = stringResource(id = R.string.lists_complete_no_pantry))
            }
        },
        title = {
            Text(
                text = stringResource(id = R.string.lists_complete_dialog_title_new),
                style = MaterialTheme.typography.titleLarge,
            )
        },
        text = {
            val spacing = LocalSpacing.current
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = spacing.small),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(spacing.small),
            ) {
                Text(
                    text = stringResource(id = R.string.lists_complete_dialog_message_new),
                    style = MaterialTheme.typography.bodyMedium,
                )
                if (state.pantryOptions.isEmpty()) {
                    Text(
                        text = stringResource(id = R.string.lists_complete_dialog_no_pantry),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                } else {
                    state.pantryOptions.forEach { option ->
                        RowOption(
                            label = option.name,
                            selected = option.id == state.selectedPantryId,
                            onClick = { onPantrySelected(option.id) },
                        )
                    }
                    if (state.errorMessageRes != null) {
                        Text(
                            text = stringResource(id = state.errorMessageRes),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
        },
    )
}

@Composable
private fun RowOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
