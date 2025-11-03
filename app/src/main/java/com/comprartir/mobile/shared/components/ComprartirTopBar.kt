package com.comprartir.mobile.shared.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.SettingsVoice
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import com.comprartir.mobile.R
import com.comprartir.mobile.core.navigation.AppDestination
import com.comprartir.mobile.core.util.FeatureFlags
import com.comprartir.mobile.core.util.IntegrationPlaceholders

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComprartirTopBar(
    destinationRoute: String?,
    showBack: Boolean,
    onBack: () -> Unit,
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

    CenterAlignedTopAppBar(
        title = { Text(text = stringResource(id = title)) },
        navigationIcon = {
            if (showBack) {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.Outlined.ArrowBack, contentDescription = stringResource(id = R.string.cd_back))
                }
            }
        },
        actions = {
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
        },
    )
}
