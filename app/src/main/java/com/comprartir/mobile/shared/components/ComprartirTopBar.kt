package com.comprartir.mobile.shared.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.SettingsVoice
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Person
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.comprartir.mobile.R
import com.comprartir.mobile.core.navigation.AppDestination
import com.comprartir.mobile.core.util.FeatureFlags
import com.comprartir.mobile.core.util.IntegrationPlaceholders
import com.comprartir.mobile.core.designsystem.LocalSpacing
import com.comprartir.mobile.core.designsystem.theme.ColorTokens

@Composable
fun ComprartirTopBar(
    destinationRoute: String?,
    showBack: Boolean,
    onBack: () -> Unit,
    onProfileClick: () -> Unit = {},
    featureFlags: FeatureFlags = FeatureFlags.Disabled,
) {
    val title = remember(destinationRoute) {
        when (destinationRoute) {
            AppDestination.Dashboard.route -> R.string.title_dashboard
            AppDestination.Lists.route -> R.string.title_lists
            AppDestination.ListDetails.route -> R.string.title_list_details
            AppDestination.Products.route -> R.string.title_products
            AppDestination.Categorize.route -> R.string.title_categorize_products
            AppDestination.Profile.route -> R.string.title_profile
            AppDestination.Settings.route -> R.string.title_settings
            AppDestination.Pantry.route -> R.string.title_pantry
            AppDestination.SignIn.route -> R.string.title_sign_in
            AppDestination.Register.route -> R.string.title_register
            AppDestination.Verify.route -> R.string.title_verify
            AppDestination.UpdatePassword.route -> R.string.title_update_password
            AppDestination.ShareList.route -> R.string.title_share_list
            AppDestination.AcquireProduct.route -> R.string.title_acquire_products
            else -> R.string.app_name
        }
    }

    val spacing = LocalSpacing.current
    Surface(
        color = ColorTokens.NavSurface,
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .statusBarsPadding()
                .padding(horizontal = spacing.large, vertical = spacing.medium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.medium),
        ) {
            if (showBack) {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.Outlined.ArrowBack, contentDescription = stringResource(id = R.string.cd_back))
                }
            }
            Text(
                text = stringResource(id = title),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 28.sp,
                ),
                modifier = Modifier.weight(1f),
            )
            if (featureFlags.rnf7Barcode) {
                IconButton(onClick = { IntegrationPlaceholders.launchBarcodeScanner() }) {
                    Icon(imageVector = Icons.Outlined.QrCodeScanner, contentDescription = stringResource(id = R.string.cd_barcode_scanner))
                }
            }
            if (featureFlags.rnf8VoiceCommands) {
                IconButton(onClick = { IntegrationPlaceholders.startVoiceCommandSession() }) {
                    Icon(imageVector = Icons.Outlined.SettingsVoice, contentDescription = stringResource(id = R.string.cd_voice_commands))
                }
            }
            if (featureFlags.rnf9PhotoCapture) {
                IconButton(onClick = { IntegrationPlaceholders.captureProductPhoto() }) {
                    Icon(imageVector = Icons.Outlined.PhotoCamera, contentDescription = stringResource(id = R.string.cd_photo_capture))
                }
            }
            
            // Profile button - only show if not already on profile screen
            if (destinationRoute != AppDestination.Profile.route) {
                IconButton(
                    onClick = onProfileClick,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape),
                ) {
                    Surface(
                        modifier = Modifier.size(36.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = stringResource(R.string.title_profile),
                            modifier = Modifier.padding(8.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }
            }
        }
    }
    Divider(
        thickness = 1.dp,
        color = ColorTokens.NavDivider,
    )
    }
