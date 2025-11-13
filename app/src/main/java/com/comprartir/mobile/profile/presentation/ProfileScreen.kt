package com.comprartir.mobile.profile.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import com.comprartir.mobile.core.designsystem.LocalSpacing
import com.comprartir.mobile.profile.domain.AppLanguage
import com.comprartir.mobile.profile.domain.AppTheme
import com.comprartir.mobile.profile.domain.ProfileField

@Composable
fun ProfileRoute(
    contentPadding: PaddingValues = PaddingValues(),
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    
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
                                    photoUrl = state.currentProfile.photoUrl,
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
                                photoUrl = state.currentProfile.photoUrl,
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
    val languageOptions = getLanguageOptions()
    val themeOptions = getThemeOptions()
    
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
        
        // Language dropdown
        ProfileDropdownField(
            value = languageOptions.find { it.first == state.currentProfile.language.code }?.second 
                ?: stringResource(R.string.language_system),
            onValueChange = { selectedCode ->
                onLanguageChanged(AppLanguage.fromCode(selectedCode))
            },
            label = stringResource(R.string.profile_language),
            options = languageOptions,
            enabled = state.isEditing,
        )
        
        // Theme dropdown
        ProfileDropdownField(
            value = themeOptions.find { it.first == state.currentProfile.theme.code }?.second 
                ?: stringResource(R.string.theme_system),
            onValueChange = { selectedCode ->
                onThemeChanged(AppTheme.fromCode(selectedCode))
            },
            label = stringResource(R.string.profile_theme),
            options = themeOptions,
            enabled = state.isEditing,
        )
    }
}

