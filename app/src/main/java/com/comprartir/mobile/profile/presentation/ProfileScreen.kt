package com.comprartir.mobile.profile.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
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
import com.comprartir.mobile.core.ui.rememberIsLandscape
import com.comprartir.mobile.core.ui.rememberIsTablet

@Composable
fun ProfileRoute(
    contentPadding: PaddingValues = PaddingValues(),
    windowSizeClass: WindowSizeClass? = null,
    navController: androidx.navigation.NavController? = null,
    viewModel: ProfileViewModel = hiltViewModel(),
    onChangePasswordClick: () -> Unit = {},
    onLogout: () -> Unit = {},
) {
    val state = viewModel.state.collectAsStateWithLifecycle()
    val uiState = state.value
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
    
    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { messageRes ->
            val message = context.getString(messageRes)
            snackbarHostState.showSnackbar(message = message)
            viewModel.onSnackbarConsumed()
        }
    }

    val isTablet = windowSizeClass?.let { rememberIsTablet(it) } ?: false

    ProfileScreen(
        state = uiState,
        snackbarHostState = snackbarHostState,
        contentPadding = contentPadding,
        isTabletLayout = isTablet,
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
    isTabletLayout: Boolean,
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
    val isLandscape = rememberIsLandscape()
    val useTwoColumnLayout = isTabletLayout || isLandscape
    
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
            if (useTwoColumnLayout) {
                ProfileTwoColumnContent(
                    state = state,
                    contentPadding = contentPadding,
                    isTablet = isTabletLayout,
                    onChangePhotoClick = onChangePhotoClick,
                    onRemoveBackgroundClick = onRemoveBackgroundClick,
                    onNameChanged = onNameChanged,
                    onSurnameChanged = onSurnameChanged,
                    onLanguageChanged = onLanguageChanged,
                    onThemeChanged = onThemeChanged,
                    onEditClick = onEditClick,
                    onCancelClick = onCancelClick,
                    onSaveClick = onSaveClick,
                    onChangePasswordClick = onChangePasswordClick,
                    onLogoutClick = onLogoutClick,
                )
            } else {
                ProfilePortraitContent(
                    state = state,
                    contentPadding = contentPadding,
                    onChangePhotoClick = onChangePhotoClick,
                    onRemoveBackgroundClick = onRemoveBackgroundClick,
                    onNameChanged = onNameChanged,
                    onSurnameChanged = onSurnameChanged,
                    onLanguageChanged = onLanguageChanged,
                    onThemeChanged = onThemeChanged,
                    onEditClick = onEditClick,
                    onCancelClick = onCancelClick,
                    onSaveClick = onSaveClick,
                    onChangePasswordClick = onChangePasswordClick,
                    onLogoutClick = onLogoutClick,
                )
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
private fun ProfilePortraitContent(
    state: ProfileUiState,
    contentPadding: PaddingValues,
    onChangePhotoClick: () -> Unit,
    onRemoveBackgroundClick: () -> Unit,
    onNameChanged: (String) -> Unit,
    onSurnameChanged: (String) -> Unit,
    onLanguageChanged: (AppLanguage) -> Unit,
    onThemeChanged: (AppTheme) -> Unit,
    onEditClick: () -> Unit,
    onCancelClick: () -> Unit,
    onSaveClick: () -> Unit,
    onChangePasswordClick: () -> Unit,
    onLogoutClick: () -> Unit,
) {
    val spacing = LocalSpacing.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(contentPadding)
            .padding(horizontal = spacing.medium, vertical = spacing.medium),
        verticalArrangement = Arrangement.spacedBy(spacing.medium),
    ) {
        ProfileDetailsCard(
            state = state,
            onChangePhotoClick = onChangePhotoClick,
            onRemoveBackgroundClick = onRemoveBackgroundClick,
            onNameChanged = onNameChanged,
            onSurnameChanged = onSurnameChanged,
            onLanguageChanged = onLanguageChanged,
            onThemeChanged = onThemeChanged,
            onEditClick = onEditClick,
            onCancelClick = onCancelClick,
            onSaveClick = onSaveClick,
        )
        ProfileSecurityActions(
            onChangePasswordClick = onChangePasswordClick,
            onLogoutClick = onLogoutClick,
        )
    }
}

@Composable
private fun ProfileTwoColumnContent(
    state: ProfileUiState,
    contentPadding: PaddingValues,
    isTablet: Boolean,
    onChangePhotoClick: () -> Unit,
    onRemoveBackgroundClick: () -> Unit,
    onNameChanged: (String) -> Unit,
    onSurnameChanged: (String) -> Unit,
    onLanguageChanged: (AppLanguage) -> Unit,
    onThemeChanged: (AppTheme) -> Unit,
    onEditClick: () -> Unit,
    onCancelClick: () -> Unit,
    onSaveClick: () -> Unit,
    onChangePasswordClick: () -> Unit,
    onLogoutClick: () -> Unit,
) {
    val spacing = LocalSpacing.current
    val horizontalPadding = if (isTablet) spacing.xl else spacing.medium
    val verticalPadding = if (isTablet) spacing.large else spacing.medium
    val columnSpacing = if (isTablet) spacing.xl else spacing.large
    val leftWeight = 0.5f
    val rightWeight = 0.5f
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(horizontal = horizontalPadding, vertical = verticalPadding),
        horizontalArrangement = Arrangement.spacedBy(columnSpacing),
    ) {
        Column(
            modifier = Modifier
                .weight(leftWeight)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(if (isTablet) spacing.large else spacing.medium),
        ) {
            ProfileOverviewCard(
                state = state,
                onChangePhotoClick = onChangePhotoClick,
                onRemoveBackgroundClick = onRemoveBackgroundClick,
            )
            ProfileSecurityActions(
                onChangePasswordClick = onChangePasswordClick,
                onLogoutClick = onLogoutClick,
            )
        }
        Column(
            modifier = Modifier
                .weight(rightWeight)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(spacing.medium),
        ) {
            ProfileDetailsCard(
                state = state,
                onChangePhotoClick = onChangePhotoClick,
                onRemoveBackgroundClick = onRemoveBackgroundClick,
                onNameChanged = onNameChanged,
                onSurnameChanged = onSurnameChanged,
                onLanguageChanged = onLanguageChanged,
                onThemeChanged = onThemeChanged,
                onEditClick = onEditClick,
                onCancelClick = onCancelClick,
                onSaveClick = onSaveClick,
                forceHorizontalLayout = true,
            )
        }
    }
}

@Composable
private fun ProfileDetailsCard(
    state: ProfileUiState,
    onChangePhotoClick: () -> Unit,
    onRemoveBackgroundClick: () -> Unit,
    onNameChanged: (String) -> Unit,
    onSurnameChanged: (String) -> Unit,
    onLanguageChanged: (AppLanguage) -> Unit,
    onThemeChanged: (AppTheme) -> Unit,
    onEditClick: () -> Unit,
    onCancelClick: () -> Unit,
    onSaveClick: () -> Unit,
    forceHorizontalLayout: Boolean = false,
) {
    val spacing = LocalSpacing.current
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val isWideScreen = forceHorizontalLayout || maxWidth > 600.dp
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
                Text(
                    text = stringResource(R.string.title_profile),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                )
                if (isWideScreen) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(spacing.large),
                    ) {
                        ProfileAvatarSection(
                            isEditing = state.isEditing,
                            onChangePhotoClick = onChangePhotoClick,
                            onRemoveBackgroundClick = onRemoveBackgroundClick,
                            modifier = Modifier.width(200.dp),
                        )
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
}

@Composable
private fun ProfileOverviewCard(
    state: ProfileUiState,
    onChangePhotoClick: () -> Unit,
    onRemoveBackgroundClick: () -> Unit,
) {
    val spacing = LocalSpacing.current
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
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.title_profile),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            ProfileAvatarSection(
                isEditing = state.isEditing,
                onChangePhotoClick = onChangePhotoClick,
                onRemoveBackgroundClick = onRemoveBackgroundClick,
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = state.currentProfile.name.ifBlank { stringResource(id = R.string.profile_first_name) },
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = state.currentProfile.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline,
                )
            }
        }
    }
}

@Composable
private fun ProfileSecurityActions(
    onChangePasswordClick: () -> Unit,
    onLogoutClick: () -> Unit,
) {
    val spacing = LocalSpacing.current
    Column(
        verticalArrangement = Arrangement.spacedBy(spacing.small),
    ) {
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
