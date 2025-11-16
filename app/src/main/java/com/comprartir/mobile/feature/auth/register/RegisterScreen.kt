package com.comprartir.mobile.feature.auth.register

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.comprartir.mobile.R
import com.comprartir.mobile.core.designsystem.ComprartirTheme
import com.comprartir.mobile.core.designsystem.LocalSpacing
import com.comprartir.mobile.core.designsystem.borderDefault
import com.comprartir.mobile.core.designsystem.brand
import com.comprartir.mobile.core.designsystem.surfaceCard
import com.comprartir.mobile.core.designsystem.textMuted
import com.comprartir.mobile.core.designsystem.textPrimary

@Composable
fun RegisterRoute(
    onNavigateToLogin: () -> Unit,
    onNavigateToVerify: (String) -> Unit,
    viewModel: RegisterViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            println("RegisterRoute: Success! Navigating to verify with email: ${state.email.trim()}")
            kotlinx.coroutines.delay(1500) // Show success banner for 1.5 seconds
            onNavigateToVerify(state.email.trim())
        }
    }

    RegisterScreen(
        state = state,
        onEvent = viewModel::onEvent,
        onNavigateToLogin = onNavigateToLogin,
    )
}

@Composable
fun RegisterScreen(
    state: RegisterUiState,
    onEvent: (RegisterEvent) -> Unit,
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current
    val focusManager = LocalFocusManager.current
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF4DA851), Color(0xFF3E8E47)),
                )
            )
            .padding(horizontal = 24.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 420.dp),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.borderDefault),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.large, vertical = spacing.extraLarge),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(spacing.large),
            ) {
                AnimatedVisibility(visible = state.isSuccess) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 1.dp,
                                color = Color(0xFFB7E4C7),
                                shape = RoundedCornerShape(16.dp),
                            )
                            .background(
                                color = Color(0xFFEAF6EA),
                                shape = RoundedCornerShape(16.dp),
                            )
                            .padding(12.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF2D6A4F),
                                modifier = Modifier.size(24.dp),
                            )
                            Text(
                                text = stringResource(id = R.string.register_success_message),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF1B4332),
                                modifier = Modifier.weight(1f),
                            )
                            IconButton(onClick = { onEvent(RegisterEvent.DismissSuccess) }) {
                                Icon(
                                    imageVector = Icons.Outlined.Close,
                                    contentDescription = stringResource(id = R.string.register_success_close),
                                    tint = Color(0xFF2D6A4F),
                                )
                            }
                        }
                    }
                }
                if (state.isSuccess) {
                    Spacer(modifier = Modifier.height(spacing.medium))
                }
                AnimatedVisibility(visible = state.errorMessage != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 1.dp,
                                color = Color(0xFFF5C2C7),
                                shape = RoundedCornerShape(16.dp),
                            )
                            .background(
                                color = Color(0xFFFDE8E8),
                                shape = RoundedCornerShape(16.dp),
                            )
                            .padding(12.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ErrorOutline,
                                contentDescription = null,
                                tint = Color(0xFFB3261E),
                                modifier = Modifier.size(24.dp),
                            )
                            Text(
                                text = state.errorMessage ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF8C1D18),
                                modifier = Modifier.weight(1f),
                            )
                            IconButton(onClick = { onEvent(RegisterEvent.DismissError) }) {
                                Icon(
                                    imageVector = Icons.Outlined.Close,
                                    contentDescription = stringResource(id = R.string.register_success_close),
                                    tint = Color(0xFFB3261E),
                                )
                            }
                        }
                    }
                }
                if (state.errorMessage != null) {
                    Spacer(modifier = Modifier.height(spacing.medium))
                }
                Image(
                    painter = painterResource(id = R.drawable.logo_comprartir),
                    contentDescription = stringResource(id = R.string.app_name),
                    modifier = Modifier.size(100.dp),
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(spacing.small),
                ) {
                    Text(
                        text = stringResource(id = R.string.register_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.textPrimary,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = stringResource(id = R.string.register_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.textMuted,
                        textAlign = TextAlign.Center,
                    )
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(spacing.medium),
                ) {
                    RegisterTextField(
                        value = state.name,
                        onValueChange = { onEvent(RegisterEvent.NameChanged(it)) },
                        label = stringResource(id = R.string.label_name),
                        placeholder = stringResource(id = R.string.hint_name),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Person,
                                contentDescription = stringResource(id = R.string.label_name),
                                tint = MaterialTheme.colorScheme.textMuted,
                            )
                        },
                        isError = state.nameError != null,
                        errorText = state.nameError,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next,
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Next) },
                        ),
                    )
                    RegisterTextField(
                        value = state.lastName,
                        onValueChange = { onEvent(RegisterEvent.LastNameChanged(it)) },
                        label = stringResource(id = R.string.label_last_name),
                        placeholder = stringResource(id = R.string.hint_last_name),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Person,
                                contentDescription = stringResource(id = R.string.label_last_name),
                                tint = MaterialTheme.colorScheme.textMuted,
                            )
                        },
                        isError = state.lastNameError != null,
                        errorText = state.lastNameError,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next,
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Next) },
                        ),
                    )
                    RegisterTextField(
                        value = state.email,
                        onValueChange = { onEvent(RegisterEvent.EmailChanged(it)) },
                        label = stringResource(id = R.string.label_email),
                        placeholder = null,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Email,
                                contentDescription = stringResource(id = R.string.label_email),
                                tint = MaterialTheme.colorScheme.textMuted,
                            )
                        },
                        isError = state.emailError != null,
                        errorText = state.emailError,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next,
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Next) },
                        ),
                    )
                    RegisterTextField(
                        value = state.password,
                        onValueChange = { onEvent(RegisterEvent.PasswordChanged(it)) },
                        label = stringResource(id = R.string.label_password),
                        placeholder = null,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Lock,
                                contentDescription = stringResource(id = R.string.label_password),
                                tint = MaterialTheme.colorScheme.textMuted,
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                    contentDescription = stringResource(id = R.string.login_toggle_password_visibility),
                                    tint = MaterialTheme.colorScheme.textMuted,
                                )
                            }
                        },
                        isError = state.passwordError != null,
                        errorText = state.passwordError,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Next,
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Next) },
                        ),
                    )
                    RegisterTextField(
                        value = state.confirmPassword,
                        onValueChange = { onEvent(RegisterEvent.ConfirmPasswordChanged(it)) },
                        label = stringResource(id = R.string.label_confirm_password),
                        placeholder = stringResource(id = R.string.hint_confirm_password),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Lock,
                                contentDescription = stringResource(id = R.string.label_confirm_password),
                                tint = MaterialTheme.colorScheme.textMuted,
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                Icon(
                                    imageVector = if (confirmPasswordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                    contentDescription = stringResource(id = R.string.login_toggle_password_visibility),
                                    tint = MaterialTheme.colorScheme.textMuted,
                                )
                            }
                        },
                        isError = state.confirmPasswordError != null,
                        errorText = state.confirmPasswordError,
                        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done,
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                if (state.isSubmitEnabled) {
                                    onEvent(RegisterEvent.Submit)
                                }
                            },
                        ),
                    )
                }

                Button(
                    onClick = { onEvent(RegisterEvent.Submit) },
                    enabled = state.isSubmitEnabled && !state.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(999.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4DA851),
                        disabledContainerColor = Color(0xFF8CC28D),
                    ),
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                            color = Color.White,
                        )
                    } else {
                        Text(
                            text = stringResource(id = R.string.register_button),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(id = R.string.register_already_have_account),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.textMuted,
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(id = R.string.register_login_link),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.brand,
                        modifier = Modifier
                            .clickable(onClick = onNavigateToLogin)
                            .semantics { role = Role.Button },
                    )
                }
            }
        }
    }
}

@Composable
private fun RegisterTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String?,
    leadingIcon: @Composable (() -> Unit),
    trailingIcon: (@Composable (() -> Unit))? = null,
    isError: Boolean,
    errorText: String?,
    keyboardOptions: KeyboardOptions,
    keyboardActions: KeyboardActions,
    visualTransformation: VisualTransformation = VisualTransformation.None,
) {
    val spacing = LocalSpacing.current
    val placeholderText = placeholder ?: label
    Column(
        verticalArrangement = Arrangement.spacedBy(spacing.tiny),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.textMuted,
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text(text = placeholderText) },
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            isError = isError,
            shape = RoundedCornerShape(50.dp),
            textStyle = MaterialTheme.typography.bodyLarge,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            visualTransformation = visualTransformation,
            colors = registerTextFieldColors(),
        )
        if (errorText != null) {
            Text(
                text = errorText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Composable
private fun registerTextFieldColors() = TextFieldDefaults.outlinedTextFieldColors(
    containerColor = MaterialTheme.colorScheme.surfaceCard,
    focusedBorderColor = MaterialTheme.colorScheme.brand,
    unfocusedBorderColor = MaterialTheme.colorScheme.borderDefault,
    focusedLabelColor = MaterialTheme.colorScheme.brand,
    unfocusedLabelColor = MaterialTheme.colorScheme.textMuted,
    cursorColor = MaterialTheme.colorScheme.brand,
)

@Preview(name = "Register – Light", showBackground = true)
@Preview(name = "Register – Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun RegisterScreenPreview() {
    ComprartirTheme {
        RegisterScreen(
            state = RegisterUiState(),
            onEvent = {},
            onNavigateToLogin = {},
        )
    }
}
