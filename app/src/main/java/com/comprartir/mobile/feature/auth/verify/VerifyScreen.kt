package com.comprartir.mobile.feature.auth.verify

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.comprartir.mobile.R
import com.comprartir.mobile.core.designsystem.ComprartirTheme
import com.comprartir.mobile.core.designsystem.LocalSpacing
import com.comprartir.mobile.core.designsystem.borderDefault
import com.comprartir.mobile.core.designsystem.brand
import com.comprartir.mobile.core.designsystem.surfaceCard
import com.comprartir.mobile.core.designsystem.textMuted
import com.comprartir.mobile.core.designsystem.textPrimary
import com.comprartir.mobile.core.designsystem.theme.LocalColorTokens
import com.comprartir.mobile.core.ui.rememberIsLandscape
import com.comprartir.mobile.core.ui.rememberIsTablet

@Composable
fun VerifyScreen(
    state: VerifyUiState,
    onEvent: (VerifyEvent) -> Unit,
    onBackToLogin: () -> Unit,
    modifier: Modifier = Modifier,
    windowSizeClass: WindowSizeClass? = null,
) {
    val spacing = LocalSpacing.current
    val isLandscape = rememberIsLandscape()
    val isTablet = windowSizeClass?.let { rememberIsTablet(it) } ?: false
    val useWideLayout = isTablet || isLandscape
    val backgroundHorizontalPadding = if (isTablet) spacing.xxl else 24.dp
    val backgroundVerticalPadding = if (isTablet) spacing.xl else 16.dp

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF4DA851), Color(0xFF3E8E47)),
                )
            )
            .padding(horizontal = backgroundHorizontalPadding, vertical = backgroundVerticalPadding),
        contentAlignment = Alignment.Center,
    ) {
        if (useWideLayout) {
            val brandingWeight = 0.45f
            val formWeight = 0.55f
            val rowSpacing = if (isTablet) spacing.xl else spacing.large
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = if (isTablet) 1280.dp else 1100.dp),
                horizontalArrangement = Arrangement.spacedBy(rowSpacing),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                VerifyBrandingPanel(
                    modifier = Modifier
                        .weight(brandingWeight)
                        .fillMaxHeight(),
                )
                VerifyCard(
                    state = state,
                    onEvent = onEvent,
                    onBackToLogin = onBackToLogin,
                    modifier = Modifier
                        .weight(formWeight)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Start,
                    isTabletLayout = isTablet,
                )
            }
        } else {
            VerifyCard(
                state = state,
                onEvent = onEvent,
                onBackToLogin = onBackToLogin,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 420.dp),
                contentAlignment = Alignment.CenterHorizontally,
                isTabletLayout = false,
            )
        }
    }
}

@Composable
private fun VerifyCard(
    state: VerifyUiState,
    onEvent: (VerifyEvent) -> Unit,
    onBackToLogin: () -> Unit,
    modifier: Modifier,
    contentAlignment: Alignment.Horizontal,
    isTabletLayout: Boolean,
) {
    val spacing = LocalSpacing.current
    val horizontalPadding = if (isTabletLayout) spacing.xl else spacing.large
    val verticalPadding = if (isTabletLayout) spacing.xxl else spacing.extraLarge
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.borderDefault),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding, vertical = verticalPadding),
            horizontalAlignment = contentAlignment,
            verticalArrangement = Arrangement.spacedBy(spacing.large),
        ) {
            val logoRes = if (LocalColorTokens.current.isDark) {
                R.drawable.logo_comprartir_nobg
            } else {
                R.drawable.logo_comprartir
            }
            Image(
                painter = painterResource(id = logoRes),
                contentDescription = stringResource(id = R.string.app_name),
                modifier = Modifier.size(100.dp),
            )
            Column(
                horizontalAlignment = contentAlignment,
                verticalArrangement = Arrangement.spacedBy(spacing.small),
            ) {
                Text(
                    text = stringResource(id = R.string.verify_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.textPrimary,
                    textAlign = if (contentAlignment == Alignment.CenterHorizontally) TextAlign.Center else TextAlign.Start,
                )
                Text(
                    text = stringResource(id = R.string.verify_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.textMuted,
                    textAlign = if (contentAlignment == Alignment.CenterHorizontally) TextAlign.Center else TextAlign.Start,
                )
            }
            if (state.errorMessage != null) {
                ErrorBanner(message = state.errorMessage) {
                    onEvent(VerifyEvent.DismissError)
                }
            }
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(spacing.medium),
            ) {
                OutlinedTextField(
                    value = state.email,
                    onValueChange = {},
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(text = stringResource(id = R.string.label_email)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Mail,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.textMuted,
                        )
                    },
                    enabled = false,
                    readOnly = true,
                    shape = RoundedCornerShape(999.dp),
                    colors = pillTextFieldColors(
                        disabledTextColor = MaterialTheme.colorScheme.textPrimary,
                        disabledLabelColor = MaterialTheme.colorScheme.textMuted,
                        disabledBorderColor = MaterialTheme.colorScheme.borderDefault,
                        disabledLeadingIconColor = MaterialTheme.colorScheme.textMuted,
                        disabledTrailingIconColor = MaterialTheme.colorScheme.textMuted,
                    ),
                )
                OutlinedTextField(
                    value = state.code,
                    onValueChange = { raw ->
                        val sanitized = raw.filter { it.isLetterOrDigit() }.take(16)
                        onEvent(VerifyEvent.CodeChanged(sanitized))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(text = stringResource(id = R.string.verify_code_label)) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Ascii,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(onDone = { onEvent(VerifyEvent.Submit) }),
                    singleLine = true,
                    isError = state.codeError != null,
                    shape = RoundedCornerShape(999.dp),
                    colors = pillTextFieldColors(),
                )
                state.codeError?.let { error ->
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
            Button(
                onClick = { onEvent(VerifyEvent.Submit) },
                enabled = !state.isLoading && state.code.length == 16,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(999.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.brand,
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
                        text = stringResource(id = R.string.verify_button),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
            Column(
                horizontalAlignment = contentAlignment,
                verticalArrangement = Arrangement.spacedBy(spacing.small),
            ) {
                val resendText = if (state.isResendEnabled) {
                    stringResource(id = R.string.verify_resend)
                } else {
                    stringResource(id = R.string.verify_resend_countdown, state.countdownSeconds)
                }
                Text(
                    text = resendText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (state.isResendEnabled) MaterialTheme.colorScheme.brand else MaterialTheme.colorScheme.textMuted,
                    modifier = Modifier
                        .semantics { role = Role.Button }
                        .clickable(enabled = state.isResendEnabled) { onEvent(VerifyEvent.ResendCode) },
                    textAlign = if (contentAlignment == Alignment.CenterHorizontally) TextAlign.Center else TextAlign.Start,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = stringResource(id = R.string.verify_back_to_login),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.brand,
                        modifier = Modifier
                            .clickable(onClick = onBackToLogin)
                            .semantics { role = Role.Button },
                    )
                }
            }
        }
    }
}

@Composable
private fun VerifyBrandingPanel(
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.borderDefault),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = spacing.extraLarge, vertical = spacing.extraLarge),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start,
        ) {
            val logoRes = if (LocalColorTokens.current.isDark) {
                R.drawable.logo_comprartir_nobg
            } else {
                R.drawable.logo_comprartir
            }
            Image(
                painter = painterResource(id = logoRes),
                contentDescription = stringResource(id = R.string.app_name),
                modifier = Modifier.size(90.dp),
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(spacing.small),
            ) {
                Text(
                    text = stringResource(id = R.string.verify_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = stringResource(id = R.string.verify_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.textMuted,
                )
                Text(
                    text = stringResource(id = R.string.verify_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.textPrimary,
                )
            }
        }
    }
}

@Composable
private fun ErrorBanner(
    message: String,
    onDismiss: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = Color(0xFFFDE8E8), shape = RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            imageVector = Icons.Outlined.ErrorOutline,
            contentDescription = null,
            tint = Color(0xFFB3261E),
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.textPrimary,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = stringResource(id = R.string.register_success_close),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.brand,
            modifier = Modifier
                .clickable(onClick = onDismiss)
                .semantics { role = Role.Button },
        )
    }
}

@Composable
private fun pillTextFieldColors(
    disabledTextColor: Color = MaterialTheme.colorScheme.onSurface,
    disabledLabelColor: Color = MaterialTheme.colorScheme.textMuted,
    disabledBorderColor: Color = MaterialTheme.colorScheme.borderDefault,
    disabledLeadingIconColor: Color = MaterialTheme.colorScheme.textMuted,
    disabledTrailingIconColor: Color = MaterialTheme.colorScheme.textMuted,
) = TextFieldDefaults.outlinedTextFieldColors(
    containerColor = MaterialTheme.colorScheme.surfaceCard,
    focusedBorderColor = MaterialTheme.colorScheme.brand,
    unfocusedBorderColor = MaterialTheme.colorScheme.borderDefault,
    focusedLabelColor = MaterialTheme.colorScheme.brand,
    unfocusedLabelColor = MaterialTheme.colorScheme.textMuted,
    cursorColor = MaterialTheme.colorScheme.brand,
    disabledTextColor = disabledTextColor,
    disabledLabelColor = disabledLabelColor,
    disabledBorderColor = disabledBorderColor,
    disabledLeadingIconColor = disabledLeadingIconColor,
    disabledTrailingIconColor = disabledTrailingIconColor,
)

@Preview(name = "Verify – Light", showBackground = true)
@Preview(name = "Verify – Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun VerifyScreenPreview() {
    ComprartirTheme {
        VerifyScreen(
            state = VerifyUiState(email = "user@example.com"),
            onEvent = {},
            onBackToLogin = {},
        )
    }
}
