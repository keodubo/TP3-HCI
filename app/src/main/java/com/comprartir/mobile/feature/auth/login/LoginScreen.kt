package com.comprartir.mobile.feature.auth.login

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.comprartir.mobile.R
import com.comprartir.mobile.core.designsystem.ComprartirTheme
import com.comprartir.mobile.core.designsystem.LocalSpacing
import com.comprartir.mobile.core.designsystem.brand
import com.comprartir.mobile.core.designsystem.brandDark
import com.comprartir.mobile.core.designsystem.borderDefault
import com.comprartir.mobile.core.designsystem.surfaceCard
import com.comprartir.mobile.core.designsystem.textMuted
import com.comprartir.mobile.core.designsystem.textPrimary
import com.comprartir.mobile.core.ui.rememberIsLandscape
import com.comprartir.mobile.core.ui.rememberIsTablet
import java.util.Locale

@Composable
fun LoginRoute(
    onRecoverPassword: () -> Unit,
    onRegister: () -> Unit,
    onSubmit: () -> Unit,
    windowSizeClass: WindowSizeClass? = null,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LoginScreen(
        state = uiState,
        onEmailChanged = { viewModel.onEvent(LoginEvent.EmailChanged(it)) },
        onPasswordChanged = { viewModel.onEvent(LoginEvent.PasswordChanged(it)) },
        onLogin = { viewModel.onEvent(LoginEvent.Submit(onSubmit)) },
        onRecoverPassword = onRecoverPassword,
        onRegister = onRegister,
        windowSizeClass = windowSizeClass,
    )
}

@Composable
fun LoginScreen(
    state: LoginUiState,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onLogin: () -> Unit,
    onRecoverPassword: () -> Unit,
    onRegister: () -> Unit,
    modifier: Modifier = Modifier,
    windowSizeClass: WindowSizeClass? = null,
) {
    val spacing = LocalSpacing.current
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.4f
    val gradientColors = if (isDarkTheme) {
        listOf(Color(0xFF3E8E47), Color(0xFF2A5932))
    } else {
        listOf(Color(0xFF4DA851), Color(0xFF3E8E47))
    }
    val locale = rememberLocale()
    val isTablet = windowSizeClass?.let { rememberIsTablet(it) } ?: false
    val containerMaxWidth = if (windowSizeClass?.widthSizeClass == WindowWidthSizeClass.Expanded) {
        600.dp
    } else {
        420.dp
    }
    val isLandscape = rememberIsLandscape()
    val useWideLayout = isTablet || isLandscape
    val wideLayoutMaxWidth = if (isTablet) 1100.dp else 980.dp
    val cardMaxHeight = if (isTablet) 600.dp else 660.dp
    val backgroundHorizontalPadding = if (isTablet) spacing.xxl else 24.dp
    val backgroundVerticalPadding = if (isTablet) spacing.xl else 16.dp

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(gradientColors))
            .padding(horizontal = backgroundHorizontalPadding, vertical = backgroundVerticalPadding),
        contentAlignment = Alignment.Center,
    ) {
        if (useWideLayout) {
            val brandingWeight = if (isTablet) 0.45f else 0.5f
            val formWeight = 1f - brandingWeight
            val horizontalSpacing = if (isTablet) spacing.xl else spacing.large
            Row(
                modifier = Modifier
                    .widthIn(max = wideLayoutMaxWidth)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(horizontalSpacing),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                LoginBrandingPanel(
                    modifier = Modifier
                        .weight(brandingWeight)
                        .heightIn(max = cardMaxHeight),
                )
                LoginFormCard(
                    state = state,
                    onEmailChanged = onEmailChanged,
                    onPasswordChanged = onPasswordChanged,
                    onLogin = onLogin,
                    onRecoverPassword = onRecoverPassword,
                    onRegister = onRegister,
                    locale = locale,
                    showBrandingHeader = false,
                    contentAlignment = Alignment.Start,
                    isTabletLayout = isTablet,
                    modifier = Modifier
                        .weight(formWeight)
                        .heightIn(max = cardMaxHeight),
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = containerMaxWidth),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(spacing.large),
            ) {
                LoginFormCard(
                    state = state,
                    onEmailChanged = onEmailChanged,
                    onPasswordChanged = onPasswordChanged,
                    onLogin = onLogin,
                    onRecoverPassword = onRecoverPassword,
                    onRegister = onRegister,
                    locale = locale,
                    showBrandingHeader = true,
                    contentAlignment = Alignment.CenterHorizontally,
                    isTabletLayout = isTablet,
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 420.dp),
                )
            }
        }
    }
}

@Composable
private fun textFieldColors() = TextFieldDefaults.outlinedTextFieldColors(
    containerColor = MaterialTheme.colorScheme.surfaceCard,
    focusedBorderColor = MaterialTheme.colorScheme.brand,
    unfocusedBorderColor = MaterialTheme.colorScheme.borderDefault,
    focusedLabelColor = MaterialTheme.colorScheme.brand,
    unfocusedLabelColor = MaterialTheme.colorScheme.textMuted,
    cursorColor = MaterialTheme.colorScheme.brand,
)

@Composable
private fun LoginFormCard(
    state: LoginUiState,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onLogin: () -> Unit,
    onRecoverPassword: () -> Unit,
    onRegister: () -> Unit,
    locale: Locale,
    showBrandingHeader: Boolean,
    contentAlignment: Alignment.Horizontal,
    isTabletLayout: Boolean,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current
    val horizontalPadding = if (isTabletLayout) spacing.xl else spacing.large
    val verticalPadding = if (isTabletLayout) spacing.xxl else spacing.small
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
            if (showBrandingHeader) {
                LoginBrandingContent(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    textAlign = TextAlign.Center,
                    showSubtitle = true,
                )
            }
            LoginFormFields(
                state = state,
                onEmailChanged = onEmailChanged,
                onPasswordChanged = onPasswordChanged,
                onLogin = onLogin,
                onRecoverPassword = onRecoverPassword,
                onRegister = onRegister,
                locale = locale,
                horizontalAlignment = contentAlignment,
            )
        }
    }
}

@Composable
private fun LoginFormFields(
    state: LoginUiState,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onLogin: () -> Unit,
    onRecoverPassword: () -> Unit,
    onRegister: () -> Unit,
    locale: Locale,
    horizontalAlignment: Alignment.Horizontal,
) {
    val spacing = LocalSpacing.current
    val isCentered = horizontalAlignment == Alignment.CenterHorizontally
    var isPasswordVisible by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = Arrangement.spacedBy(spacing.medium),
    ) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = state.email,
            onValueChange = onEmailChanged,
            singleLine = true,
            placeholder = { Text(text = stringResource(id = R.string.label_email)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Mail,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.textMuted,
                )
            },
            textStyle = MaterialTheme.typography.bodyLarge,
            shape = RoundedCornerShape(999.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
            ),
            colors = textFieldColors(),
        )
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = state.password,
            onValueChange = onPasswordChanged,
            singleLine = true,
            placeholder = { Text(text = stringResource(id = R.string.label_password)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.textMuted,
                )
            },
            trailingIcon = {
                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                    Icon(
                        imageVector = if (isPasswordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                        contentDescription = stringResource(id = R.string.login_toggle_password_visibility),
                        tint = MaterialTheme.colorScheme.textMuted,
                    )
                }
            },
            textStyle = MaterialTheme.typography.bodyLarge,
            visualTransformation = if (isPasswordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            shape = RoundedCornerShape(999.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(
                onDone = { onLogin() },
            ),
            colors = textFieldColors(),
        )
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            onClick = onLogin,
            shape = RoundedCornerShape(999.dp),
            enabled = state.isLoginEnabled && !state.isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFA5D8A5),
                contentColor = MaterialTheme.colorScheme.textPrimary,
                disabledContainerColor = MaterialTheme.colorScheme.textMuted.copy(alpha = 0.2f),
                disabledContentColor = MaterialTheme.colorScheme.textMuted,
            ),
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.brandDark,
                )
            } else {
                Text(
                    text = stringResource(id = R.string.action_sign_in).uppercase(locale),
                    style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 1.5.sp),
                )
            }
        }
        state.errorMessage?.let { message ->
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                textAlign = if (isCentered) TextAlign.Center else TextAlign.Start,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(spacing.small),
            horizontalAlignment = horizontalAlignment,
        ) {
            LinkRow(
                prompt = stringResource(id = R.string.login_forgot_prompt),
                action = stringResource(id = R.string.login_forgot_action),
                onClick = onRecoverPassword,
                centered = isCentered,
            )
            LinkRow(
                prompt = stringResource(id = R.string.login_register_prompt),
                action = stringResource(id = R.string.login_register_action),
                onClick = onRegister,
                centered = isCentered,
            )
        }
    }
}

@Composable
private fun LoginBrandingPanel(
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
            LoginBrandingContent(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start,
                textAlign = TextAlign.Start,
                showSubtitle = false,
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(spacing.small),
                horizontalAlignment = Alignment.Start,
            ) {
                Text(
                    text = stringResource(id = R.string.login_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.textMuted,
                    textAlign = TextAlign.Start,
                )
                Text(
                    text = stringResource(id = R.string.login_register_prompt),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.textPrimary,
                    textAlign = TextAlign.Start,
                )
            }
        }
    }
}

@Composable
private fun LoginBrandingContent(
    modifier: Modifier = Modifier,
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    textAlign: TextAlign = TextAlign.Center,
    showSubtitle: Boolean = true,
) {
    val spacing = LocalSpacing.current
    val logoRes = R.drawable.logo_comprartir_nobg
    Column(
        modifier = modifier,
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = Arrangement.spacedBy(spacing.small),
    ) {
        Image(
            painter = painterResource(id = logoRes),
            contentDescription = stringResource(id = R.string.cd_logo_comprartir),
            modifier = Modifier.size(100.dp),
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(spacing.small),
            horizontalAlignment = horizontalAlignment,
        ) {
            Text(
                text = stringResource(id = R.string.login_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.textPrimary,
                textAlign = textAlign,
            )
            if (showSubtitle) {
                Text(
                    text = stringResource(id = R.string.login_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.textMuted,
                    textAlign = textAlign,
                )
            }
        }
    }
}

@Composable
private fun LinkRow(
    prompt: String,
    action: String,
    onClick: () -> Unit,
    centered: Boolean = true,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (centered) Arrangement.Center else Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = prompt,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.textMuted,
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = action,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.brand,
            modifier = Modifier
                .clickable(onClick = onClick)
                .semantics { role = Role.Button },
        )
    }
}

@Composable
private fun rememberLocale(): Locale {
    val context = LocalContext.current
    return remember(context) {
        val configuration = context.resources.configuration
        @Suppress("DEPRECATION")
        val primary = if (!configuration.locales.isEmpty) {
            configuration.locales[0]
        } else {
            configuration.locale
        }
        primary ?: Locale.getDefault()
    }
}

@Preview(name = "Login – Light", uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Composable
private fun LoginScreenPreviewLight() {
    ComprartirTheme(darkTheme = false) {
        LoginScreen(
            state = LoginUiState(),
            onEmailChanged = {},
            onPasswordChanged = {},
            onLogin = {},
            onRecoverPassword = {},
            onRegister = {},
        )
    }
}

@Preview(name = "Login – Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun LoginScreenPreviewDark() {
    ComprartirTheme(darkTheme = true) {
        LoginScreen(
            state = LoginUiState(),
            onEmailChanged = {},
            onPasswordChanged = {},
            onLogin = {},
            onRecoverPassword = {},
            onRegister = {},
        )
    }
}
