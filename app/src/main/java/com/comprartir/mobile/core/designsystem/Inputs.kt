package com.comprartir.mobile.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
// Avoid importing KeyboardOptions/KeyboardActions at the top-level to reduce
// the chance kapt/stub generation fails to resolve these types; reference
// them by fully-qualified name inside the implementation instead.
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun ComprartirOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    singleLine: Boolean = true,
    textStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    // Keep keyboard behavior internal to avoid exposing Compose UI input types in the
    // public API (this prevents kapt/stub generation from requiring those types).
    visualTransformation: VisualTransformation = VisualTransformation.None,
    isError: Boolean = false,
    supportingText: @Composable (() -> Unit)? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val focusTint = if (isFocused) ComprartirColors.FocusHalo else Color.Transparent

    Box(
        modifier = modifier
            .background(color = focusTint, shape = ComprartirPillShape)
            .padding(2.dp),
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 44.dp),
            label = label,
            placeholder = placeholder,
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            singleLine = singleLine,
            textStyle = textStyle,
            visualTransformation = visualTransformation,
            shape = ComprartirPillShape,
            isError = isError,
            // Use the default outlined text field colors for compatibility across Material3 versions.
            // Some named parameters changed between library versions; using the defaults avoids
            // compilation failures caused by signature differences.
            colors = TextFieldDefaults.outlinedTextFieldColors(),
            interactionSource = interactionSource,
            supportingText = supportingText,
        )
    }
}
