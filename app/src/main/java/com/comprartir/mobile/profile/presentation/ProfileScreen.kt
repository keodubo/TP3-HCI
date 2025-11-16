package com.comprartir.mobile.profile.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.comprartir.mobile.R
import com.comprartir.mobile.core.data.datastore.AppTheme
import com.comprartir.mobile.core.designsystem.LocalSpacing
import com.comprartir.mobile.profile.domain.AppLanguage
import com.comprartir.mobile.profile.domain.ProfileField
import com.comprartir.mobile.shared.i18n.rememberLanguageOptions

@Composable
fun ProfileRoute(
    contentPadding: PaddingValues = PaddingValues(),
    navController: androidx.navigation.NavController? = null,
    viewModel: ProfileViewModel = hiltViewModel(),
    onChangePasswordClick: () -> Unit = {},
    onLogout: () -> Unit = {},
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    
    // Handle password change success from ChangePasswordScreen
    LaunchedEffect(navController) {
        navController?.currentBackStackEntry
            ?.savedStateHandle
            ?.getStateFlow("password_changed", false)
            ?.collect { passwordChanged ->
                if (passwordChanged) {
                    // Clear the flag immediately to prevent re-showing on config changes
                    navController.currentBackStackEntry
                        ?.savedStateHandle
                        ?.set("password_changed", false)
                    
                    // Show success message
                    val message = context.getString(R.string.change_password_success)
                    snackbarHostState.showSnackbar(message = message)
                }
            }
    }
    
    LaunchedEffect(state.snackbarMessage) {
        state.snackbarMessage?.let { messageRes ->
            val message = context.getString(messageRes)
            snackbarHostState.showSnackbar(message = message)
            viewModel.onSnackbarConsumed()
        }
    }

    ProfileScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        contentPadding = contentPadding,
        onEditClick = viewModel::onEditClicked,
        onCancelClick = viewModel::onCancelEdit,
        onSaveClick = viewModel::onSaveClicked,
        onNameChanged = viewModel::onNameChanged,
        onSurnameChanged = viewModel::onSurnameChanged,
        onLanguageChanged = viewModel::onLanguageChanged,
        onThemeChanged = viewModel::onThemeChanged,
        onChangePhotoClick = { /* TODO: Implement photo picker */ },
        onRemoveBackgroundClick = { /* TODO: Implement when backend supports */ },
        onChangePasswordClick = onChangePasswordClick,
        onLogoutClick = { viewModel.onLogout(onLogout) },
    )
}

@Composable
fun ProfileScreen(
    state: ProfileUiState,
    snackbarHostState: SnackbarHostState,
    contentPadding: PaddingValues,
    onEditClick: () -> Unit,
    onCancelClick: () -> Unit,
    onSaveClick: () -> Unit,
    onNameChanged: (String) -> Unit,
    onSurnameChanged: (String) -> Unit,
    onLanguageChanged: (AppLanguage) -> Unit,
    onThemeChanged: (AppTheme) -> Unit,
    onChangePhotoClick: () -> Unit,
    onRemoveBackgroundClick: () -> Unit,
    onChangePasswordClick: () -> Unit,
    onLogoutClick: () -> Unit,
) {
    val spacing = LocalSpacing.current
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        if (state.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(contentPadding)
                    .padding(
                        horizontal = spacing.medium,
                        vertical = spacing.medium,
                    ),
                verticalArrangement = Arrangement.spacedBy(spacing.medium),
            ) {
                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    val isWideScreen = maxWidth > 600.dp
                    
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(spacing.large),
                            verticalArrangement = Arrangement.spacedBy(spacing.medium),
                        ) {
                            // Header with title
                            Text(
                                text = stringResource(R.string.title_profile),
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.SemiBold,
                            )
                            
                            if (isWideScreen) {
                            // Wide screen: Avatar on left, fields on right
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(spacing.large),
                            ) {
                                // Avatar section
                                ProfileAvatarSection(
                                    isEditing = state.isEditing,
                                    onChangePhotoClick = onChangePhotoClick,
                                    onRemoveBackgroundClick = onRemoveBackgroundClick,
                                    modifier = Modifier.width(180.dp),
                                )
                                
                                // Fields section
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(spacing.medium),
                                ) {
                                    ProfileFields(
                                        state = state,
                                        onNameChanged = onNameChanged,
                                        onSurnameChanged = onSurnameChanged,
                                        onLanguageChanged = onLanguageChanged,
                                        onThemeChanged = onThemeChanged,
                                    )
                                }
                            }
                        } else {
                            // Narrow screen: Avatar on top, fields below
                            ProfileAvatarSection(
                                isEditing = state.isEditing,
                                onChangePhotoClick = onChangePhotoClick,
                                onRemoveBackgroundClick = onRemoveBackgroundClick,
                                modifier = Modifier.fillMaxWidth(),
                            )
                            
                            ProfileFields(
                                state = state,
                                onNameChanged = onNameChanged,
                                onSurnameChanged = onSurnameChanged,
                                onLanguageChanged = onLanguageChanged,
                                onThemeChanged = onThemeChanged,
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(spacing.small))
                        
                            // Action buttons
                            ProfileActionButtons(
                                isEditing = state.isEditing,
                                isSaving = state.isSaving,
                                canSave = state.hasUnsavedChanges && state.fieldErrors.isEmpty(),
                                onEditClick = onEditClick,
                                onSaveClick = onSaveClick,
                                onCancelClick = onCancelClick,
                            )
                        }
                    }
                }

                // Security section
                Spacer(modifier = Modifier.height(spacing.medium))

                OutlinedButton(
                    onClick = onChangePasswordClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(999.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary,
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                ) {
                    Text(text = stringResource(R.string.profile_change_password_button))
                }

                // Logout button at the bottom
                Spacer(modifier = Modifier.height(spacing.small))

                OutlinedButton(
                    onClick = onLogoutClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(999.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error,
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                ) {
                    Text(text = stringResource(R.string.action_logout))
                }
            }
        }
        
        // Snackbar at bottom
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun ProfileFields(
    state: ProfileUiState,
    onNameChanged: (String) -> Unit,
    onSurnameChanged: (String) -> Unit,
    onLanguageChanged: (AppLanguage) -> Unit,
    onThemeChanged: (AppTheme) -> Unit,
) {
    val spacing = LocalSpacing.current
    val languageOptions = rememberLanguageOptions()
    val themeOptions = listOf(
        AppTheme.LIGHT to stringResource(R.string.theme_light),
        AppTheme.DARK to stringResource(R.string.theme_dark),
    )
    
    Column(
        verticalArrangement = Arrangement.spacedBy(spacing.medium),
    ) {
        // Name field
        ProfileTextField(
            value = state.currentProfile.name,
            onValueChange = onNameChanged,
            label = stringResource(R.string.profile_first_name),
            enabled = state.isEditing,
            errorMessage = state.fieldErrors[ProfileField.NAME]?.let { stringResource(it) },
        )
        
        // Surname field
        ProfileTextField(
            value = state.currentProfile.surname,
            onValueChange = onSurnameChanged,
            label = stringResource(R.string.profile_last_name),
            enabled = state.isEditing,
            errorMessage = state.fieldErrors[ProfileField.SURNAME]?.let { stringResource(it) },
        )
        
        // Email field (always disabled)
        ProfileTextField(
            value = state.currentProfile.email,
            onValueChange = {},
            label = stringResource(R.string.profile_email_label),
            enabled = false,
            supportingText = stringResource(R.string.profile_email_hint),
        )
        
        ProfileDropdownField(
            value = languageOptions.firstOrNull { it.language == state.currentProfile.language }?.label
                ?: stringResource(R.string.language_system),
            onValueChange = { selectedCode ->
                val selectedLanguage = languageOptions.firstOrNull { it.language.code == selectedCode }?.language
                    ?: AppLanguage.SYSTEM
                onLanguageChanged(selectedLanguage)
            },
            label = stringResource(R.string.profile_language),
            options = languageOptions.map { it.language.code to it.label },
            enabled = true, // Preferencias siempre editables
        )

        ProfileDropdownField(
            value = themeOptions.firstOrNull { it.first == state.themePreference }?.second
                ?: stringResource(R.string.theme_light),
            onValueChange = { selectedCode ->
                val selectedTheme = AppTheme.entries.firstOrNull { it.storageValue == selectedCode }
                    ?: AppTheme.LIGHT
                onThemeChanged(selectedTheme)
            },
            label = stringResource(R.string.profile_theme),
            options = themeOptions.map { it.first.storageValue to it.second },
            enabled = true,
        )
    }
}
