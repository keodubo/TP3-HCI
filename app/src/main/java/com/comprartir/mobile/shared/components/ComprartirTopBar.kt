package com.comprartir.mobile.shared.components

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.SettingsVoice
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.comprartir.mobile.R
import com.comprartir.mobile.core.designsystem.LocalSpacing
import com.comprartir.mobile.core.designsystem.theme.ColorTokens
import com.comprartir.mobile.core.navigation.AppDestination
import com.comprartir.mobile.core.util.FeatureFlags
import com.comprartir.mobile.core.util.IntegrationPlaceholders

@Composable
fun ComprartirTopBar(
    destinationRoute: String?,
    showBack: Boolean,
    onBack: () -> Unit,
    onProfileClick: () -> Unit = {},
    featureFlags: FeatureFlags = FeatureFlags.Disabled,
) {
    val title = remember(destinationRoute) {
        when {
            destinationRoute == AppDestination.Dashboard.route -> R.string.title_dashboard
            // Lists index: "lists/manage" or "lists/manage?..." -> "Shopping lists"
            destinationRoute?.startsWith("lists/manage") == true -> R.string.title_lists
            // List details: "lists/123" where 123 is the listId -> "List Details"
            destinationRoute?.matches(Regex("lists/\\d+")) == true -> R.string.title_list_details
            destinationRoute == AppDestination.Products.route -> R.string.title_products
            destinationRoute == AppDestination.Categorize.route -> R.string.title_categorize_products
            destinationRoute == AppDestination.Categories.route -> R.string.title_categories
            destinationRoute == AppDestination.Profile.route -> R.string.title_profile
            destinationRoute == AppDestination.Settings.route -> R.string.title_settings
            destinationRoute == AppDestination.Pantry.route -> R.string.title_pantry
            destinationRoute == AppDestination.SignIn.route -> R.string.title_sign_in
            destinationRoute == AppDestination.Register.route -> R.string.title_register
            destinationRoute == AppDestination.Verify.route -> R.string.title_verify
            destinationRoute == AppDestination.UpdatePassword.route -> R.string.title_update_password
            destinationRoute == AppDestination.ShareList.route -> R.string.title_share_list
            destinationRoute == AppDestination.AcquireProduct.route -> R.string.title_acquire_products
            else -> R.string.app_name
        }
    }

    val spacing = LocalSpacing.current
    val context = LocalContext.current
    var showBarcodeDialog by rememberSaveable { mutableStateOf(false) }
    var showVoiceDialog by rememberSaveable { mutableStateOf(false) }
    var showPhotoDialog by rememberSaveable { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val listener = object : IntegrationPlaceholders.Listener {
            override fun onBarcodeRequested() {
                showBarcodeDialog = true
            }

            override fun onVoiceRequested() {
                showVoiceDialog = true
            }

            override fun onPhotoRequested() {
                showPhotoDialog = true
            }
        }
        IntegrationPlaceholders.registerListener(listener)
        onDispose { IntegrationPlaceholders.unregisterListener(listener) }
    }

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
                        color = MaterialTheme.colorScheme.surfaceVariant,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = stringResource(R.string.title_profile),
                            modifier = Modifier.padding(8.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
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

    if (showBarcodeDialog) {
        BarcodeSimulationDialog(
            onDismiss = { showBarcodeDialog = false },
            onSimulate = { code ->
                Toast.makeText(context, "Código escaneado: $code", Toast.LENGTH_LONG).show()
                showBarcodeDialog = false
            },
        )
    }
    if (showVoiceDialog) {
        VoiceCommandDialog(
            onDismiss = { showVoiceDialog = false },
            onSubmit = { command ->
                Toast.makeText(context, "Comando de voz: $command", Toast.LENGTH_LONG).show()
                showVoiceDialog = false
            },
        )
    }
    if (showPhotoDialog) {
        PhotoCaptureDialog(
            onDismiss = { showPhotoDialog = false },
            onCapture = {
                Toast.makeText(context, "Foto simulada adjuntada", Toast.LENGTH_LONG).show()
                showPhotoDialog = false
            },
        )
    }
}

@Composable
private fun BarcodeSimulationDialog(
    onDismiss: () -> Unit,
    onSimulate: (String) -> Unit,
) {
    val spacing = LocalSpacing.current
    var barcode by rememberSaveable { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Simulador de codigo de barras") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.small)) {
                Text(text = "Ingresa un codigo o utiliza el ejemplo para simular un escaneo.")
                OutlinedTextField(
                    value = barcode,
                    onValueChange = { barcode = it },
                    label = { Text("Codigo de barras") },
                    singleLine = true,
                )
                TextButton(onClick = { barcode = SAMPLE_BARCODE }) {
                    Text(text = "Usar $SAMPLE_BARCODE")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSimulate(barcode) },
                enabled = barcode.isNotBlank(),
            ) {
                Text(text = "Simular")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancelar")
            }
        },
    )
}

@Composable
private fun VoiceCommandDialog(
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit,
) {
    val spacing = LocalSpacing.current
    var command by rememberSaveable { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Simulador de voz") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.small)) {
                Text(text = "Imagina que el dispositivo esta escuchando. Dicta un comando o usa el ejemplo.")
                OutlinedTextField(
                    value = command,
                    onValueChange = { command = it },
                    label = { Text("Comando reconocido") },
                )
                TextButton(onClick = { command = SAMPLE_VOICE_COMMAND }) {
                    Text(text = "Ejemplo: \"$SAMPLE_VOICE_COMMAND\"")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSubmit(command) },
                enabled = command.isNotBlank(),
            ) {
                Text(text = "Enviar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancelar")
            }
        },
    )
}

@Composable
private fun PhotoCaptureDialog(
    onDismiss: () -> Unit,
    onCapture: () -> Unit,
) {
    val spacing = LocalSpacing.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Captura de foto simulada") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.small)) {
                Text(text = "Esta es una vista previa de como se integrara la camara. Podes adjuntar una imagen ficticia para continuar.")
                Text(text = "No se abrira la camara real; usaremos una imagen de muestra.")
            }
        },
        confirmButton = {
            TextButton(onClick = onCapture) {
                Text(text = "Usar imagen de ejemplo")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cerrar")
            }
        },
    )
}

private const val SAMPLE_BARCODE = "1234567890123"
private const val SAMPLE_VOICE_COMMAND = "Agregar leche a la lista de compras"
